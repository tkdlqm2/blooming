require("@nomicfoundation/hardhat-toolbox");
require("dotenv").config();

/** @type import('hardhat/config').HardhatUserConfig */
module.exports = {
  solidity: {
    version: "0.8.20",
    settings: {
      optimizer: {
        enabled: true,
        runs: 200,
      },
    },
  },
  networks: {
    // 로컬 Hardhat 네트워크
    hardhat: {
      chainId: 31337,
    },
    // Ganache 로컬 네트워크 (환경 변수가 설정된 경우에만 활성화)
    // ...(process.env.PRIVATE_KEY && {
    //   ganache: {
    //     url: "http://127.0.0.1:7545",
    //     accounts: [process.env.PRIVATE_KEY],
    //     chainId: 1337,
    //   },
    // }),
    // Sepolia 테스트넷 (환경 변수가 설정된 경우에만 활성화)
    ...(process.env.PRIVATE_KEY && {
      sepolia: {
        url: "https://ethereum-sepolia.publicnode.com",
        accounts: [process.env.PRIVATE_KEY],
        chainId: 11155111,
      },
    }),
    // Goerli 테스트넷 (환경 변수가 설정된 경우에만 활성화)
    ...(process.env.PRIVATE_KEY && process.env.INFURA_PROJECT_ID && {
      goerli: {
        url: `https://goerli.infura.io/v3/${process.env.INFURA_PROJECT_ID}`,
        accounts: [process.env.PRIVATE_KEY],
        chainId: 5,
      },
    }),
  },
  etherscan: {
    apiKey: process.env.ETHERSCAN_API_KEY || "",
  },
};
