// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";
import "@openzeppelin/contracts/token/ERC20/extensions/ERC20Votes.sol";
import "@openzeppelin/contracts/token/ERC20/extensions/ERC20Permit.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/Pausable.sol";
import "@openzeppelin/contracts/utils/ReentrancyGuard.sol";

/**
 * @title GovernanceToken
 * @dev ERC20 토큰 + 거버넌스 기능 + 포인트 교환 기능 + 자동 위임 기능
 */
contract GovernanceToken is ERC20, ERC20Votes, ERC20Permit, Ownable, Pausable, ReentrancyGuard {
    
    // 포인트 교환 관련 변수
    uint256 public constant EXCHANGE_RATE = 100; // 100 포인트 = 1 토큰
    uint256 public constant DECIMALS_FACTOR = 10**18;
    
    // 포인트 교환 권한을 가진 주소들
    mapping(address => bool) public authorizedExchangers;
    
    // 사용자별 교환 기록
    mapping(address => uint256) public totalExchanged;
    
    // 자동 위임 설정
    bool public autoDelegationEnabled = true;
    mapping(address => bool) public autoDelegationExempt;
    
    // 이벤트
    event PointsExchanged(address indexed user, uint256 points, uint256 tokens);
    event ExchangerAdded(address indexed exchanger);
    event ExchangerRemoved(address indexed exchanger);
    event TokensBurned(address indexed user, uint256 amount);
    event AutoDelegationToggled(bool enabled);
    event AutoDelegationExemptSet(address indexed user, bool exempt);
    event AutoDelegated(address indexed user, uint256 amount);
    
    // 에러
    error UnauthorizedExchanger();
    error InvalidAmount();
    error ExchangePaused();
    
    constructor(
        string memory name,
        string memory symbol,
        uint256 initialSupply
    ) 
        ERC20(name, symbol) 
        ERC20Permit(name)
        Ownable(msg.sender)
    {
        _mint(msg.sender, initialSupply * DECIMALS_FACTOR);
        
        // 컨트랙트 소유자를 기본 교환 권한자로 설정
        authorizedExchangers[msg.sender] = true;
        emit ExchangerAdded(msg.sender);
        
        // 소유자에게 자동 위임
        if (autoDelegationEnabled) {
            _delegate(msg.sender, msg.sender);
            emit AutoDelegated(msg.sender, initialSupply * DECIMALS_FACTOR);
        }
    }
    
    /**
     * @dev 자동 위임이 포함된 토큰 전송 함수
     */
    function transferWithAutoDelegation(address to, uint256 amount) external returns (bool) {
        bool success = transfer(to, amount);
        
        // 전송이 성공하고 자동 위임이 활성화된 경우
        if (success && autoDelegationEnabled && !autoDelegationExempt[to] && to != address(0)) {
            // 수신자가 아직 위임하지 않은 경우에만 자동 위임
            if (delegates(to) == address(0)) {
                _delegate(to, to);
                emit AutoDelegated(to, amount);
            }
        }
        
        return success;
    }
    
    /**
     * @dev 자동 위임이 포함된 토큰 전송 함수 (from)
     */
    function transferFromWithAutoDelegation(address from, address to, uint256 amount) external returns (bool) {
        bool success = transferFrom(from, to, amount);
        
        // 전송이 성공하고 자동 위임이 활성화된 경우
        if (success && autoDelegationEnabled && !autoDelegationExempt[to] && to != address(0)) {
            // 수신자가 아직 위임하지 않은 경우에만 자동 위임
            if (delegates(to) == address(0)) {
                _delegate(to, to);
                emit AutoDelegated(to, amount);
            }
        }
        
        return success;
    }
    
    /**
     * @dev 자동 위임 기능 토글 (소유자만)
     */
    function toggleAutoDelegation() external onlyOwner {
        autoDelegationEnabled = !autoDelegationEnabled;
        emit AutoDelegationToggled(autoDelegationEnabled);
    }
    
    /**
     * @dev 특정 주소의 자동 위임 면제 설정 (소유자만)
     */
    function setAutoDelegationExempt(address user, bool exempt) external onlyOwner {
        autoDelegationExempt[user] = exempt;
        emit AutoDelegationExemptSet(user, exempt);
    }
    
    /**
     * @dev 포인트를 토큰으로 교환 (자동 위임 포함)
     */
    function exchangePointsForTokens(
        address user, 
        uint256 points
    ) external nonReentrant whenNotPaused {
        if (!authorizedExchangers[msg.sender]) {
            revert UnauthorizedExchanger();
        }
        
        if (points == 0) {
            revert InvalidAmount();
        }
        
        // 포인트를 토큰으로 변환 (100 포인트 = 1 토큰)
        uint256 tokensToMint = (points * DECIMALS_FACTOR) / EXCHANGE_RATE;
        
        // 토큰 발행 (자동 위임은 _afterTokenTransfer에서 처리됨)
        _mint(user, tokensToMint);
        
        // 교환 기록 업데이트
        totalExchanged[user] += points;
        
        emit PointsExchanged(user, points, tokensToMint);
    }
    
    /**
     * @dev 토큰을 소각 (필요한 경우)
     */
    function burnTokens(uint256 amount) external {
        if (amount == 0) {
            revert InvalidAmount();
        }
        
        _burn(msg.sender, amount);
        emit TokensBurned(msg.sender, amount);
    }
    
    /**
     * @dev 교환 권한자 추가
     */
    function addExchanger(address exchanger) external onlyOwner {
        authorizedExchangers[exchanger] = true;
        emit ExchangerAdded(exchanger);
    }
    
    /**
     * @dev 교환 권한자 제거
     */
    function removeExchanger(address exchanger) external onlyOwner {
        authorizedExchangers[exchanger] = false;
        emit ExchangerRemoved(exchanger);
    }
    
    /**
     * @dev 컨트랙트 일시정지
     */
    function pause() external onlyOwner {
        _pause();
    }
    
    /**
     * @dev 컨트랙트 일시정지 해제
     */
    function unpause() external onlyOwner {
        _unpause();
    }
    
    /**
     * @dev 사용자의 현재 투표권 조회
     */
    function getCurrentVotes(address account) external view returns (uint256) {
        return getVotes(account);
    }
    
    /**
     * @dev 특정 블록에서의 투표권 조회
     */
    function getPastVotes(address account, uint256 blockNumber) public view override returns (uint256) {
        return super.getPastVotes(account, blockNumber);
    }
    
    // Override required functions
    function _update(
        address from,
        address to,
        uint256 amount
    ) internal override(ERC20, ERC20Votes) whenNotPaused {
        super._update(from, to, amount);
    }
    
    function nonces(address owner) public view override(ERC20Permit, Nonces) returns (uint256) {
        return super.nonces(owner);
    }
} 