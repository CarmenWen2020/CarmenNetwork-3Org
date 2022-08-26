# requirment
I am running in Apple M1 Pro, everything in localhost.

Before we start with build up our Channel, we need Docker and some corresponding images install.

* install Docker Desktop
* install images (from hyperledger fabric offical website)

```
curl -sSL https://bit.ly/2ysbOFE | bash -s
```
* Two extra images

``` 
docker pull --platform linux/amd64 hyperledger/fabric-javaenv:2.4
docker pull --platform linux/amd64 hyperledger/fabric-nodeenv:2.4
```
(If you running in different envirment like WINDOWS system, you may have different images version such as *fabric-javaenv:2.4.1* is valid in window, and also you need to edit the envirment to corresponding yaml file)


* extra port number edit in local document 

```
nano /etc/hosts
``` 
	
![](/Users/carmenw/Desktop/screenshot/portnum.png)

(My IP address is 172.23.81.8, If you are under different wifi, you need to change to your IP address, and also you need to change this address to the corresponding yaml file)


That's all images we have now:

```
docker image ls
```
![](/Users/carmenw/Desktop/screenshot/images.png)





# shortcut
shut down yaml document

```
docker-compose -f docker-compose-orderer-3org.yaml down -v
docker-compose -f docker-compose-org1-2peer.yaml down -v
docker-compose -f docker-compose-org2-2peer.yaml down -v
docker-compose -f docker-compose-org3-2peer.yaml down -v
```

# Generated identity certificates
## Generate an organizational identity file 
* Goes to *bin* directory  

```
cd bin
```
* Generate an organizational identity file from the *crypto-config.yaml* configuration file and save it under ../crypto-config directory

```
./cryptogen generate --config=../crypto-config.yaml --output ../crypto-config
```
* Generate genesis block

```
./configtxgen -configPath ../config  -profile ThreeOrgsOrdererGenesis -channelID fabric-channel -outputBlock ../channel-artifacts/orderer.genesis.block
```
* Generate channel file

```
./configtxgen -configPath ../config  -profile ThreeOrgsChannel -channelID carmenchannel-3org -outputCreateChannelTx ../channel-artifacts/carmenchannel-3org.tx
```
* Generate three anchor peers configuration updated file

```
./configtxgen -configPath ../config  -profile ThreeOrgsChannel -channelID carmenchannel-3org -asOrg Org1MSP -outputAnchorPeersUpdate ../channel-artifacts/Org1MSPanchors.tx
./configtxgen -configPath ../config  -profile ThreeOrgsChannel -channelID carmenchannel-3org -asOrg Org2MSP -outputAnchorPeersUpdate ../channel-artifacts/Org2MSPanchors.tx
./configtxgen -configPath ../config  -profile ThreeOrgsChannel -channelID carmenchannel-3org -asOrg Org3MSP -outputAnchorPeersUpdate ../channel-artifacts/Org3MSPanchors.tx`
```
* Go back one level up directory

```
cd ..
```
* now you can see the file under channel-artifacts directory by enter *tree channel-artifacts*

```
channel-artifacts
├── Org1MSPanchors.tx
├── Org2MSPanchors.tx
├── Org3MSPanchors.tx
├── carmenchannel-3org.tx
└── orderer.genesis.block
```


# Start the channel
## Created Channel
* now create the channel using four yaml file:   
docker-compose-orderer-3org.yaml, docker-compose-org1-2peer.yaml, docker-compose-org2-2peer.yaml & docker-compose-org3-2peer.yaml 
![](/Users/carmenw/Desktop/screenshot/22.png)      

```
docker-compose -f docker-compose-orderer-3org.yaml up -d
docker-compose -f docker-compose-org1-2peer.yaml up -d
docker-compose -f docker-compose-org2-2peer.yaml up -d
docker-compose -f docker-compose-org3-2peer.yaml up -d
```


* container in docker is sucessfully created
![](/Users/carmenw/Desktop/screenshot/23.png)

## Goes to **fabric-cli** container
 ( to exit the container by enter *exit*)

```
docker exec -it fabric-cli bash
```
Entry channel-artifacts direction and generated *carmenchannel-3org.block*

```
cd /tmp/channel-artifacts/

export APP_CHANNEL=carmenchannel-3org
export TIMEOUT=30
export CORE_PEER_LOCALMSPID=Org1MSP
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.carmenw.com/users/Admin@org1.carmenw.com/msp

peer channel create -o orderer.carmenw.com:7050 -c ${APP_CHANNEL} -f "/tmp/channel-artifacts/$APP_CHANNEL.tx" --timeout "${TIMEOUT}s" --tls --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/carmenw.com/orderers/orderer.carmenw.com/msp/tlscacerts/tlsca.carmenw.com-cert.pem

```
the output should be looks like

```
[040 06-01 03:38:17.71 UTC] [channelCmd] InitCmdFactory -> INFO Endorser and orderer connections initialized
[041 06-01 03:38:17.92 UTC] [msp.identity] Sign -> DEBU Sign: plaintext: 0AF6060A1E08051A0608A9BEDB940622...3502A7FD89C012080A021A0012021A00 
[042 06-01 03:38:17.92 UTC] [msp.identity] Sign -> DEBU Sign: digest: EF3EE6B67BF6D9EF014296630E39515586DAF3D833D9FA26F9B2A0A4B92245FB 
[043 06-01 03:38:17.96 UTC] [cli.common] readBlock -> INFO Received block: 0
```
![](/Users/carmenw/Desktop/screenshot/24.png)



## Joined Channel
This exection also under **fabric-cli** container

**org1-peer0 joined to channel**  

```
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.carmenw.com/peers/peer0.org1.carmenw.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.carmenw.com/users/Admin@org1.carmenw.com/msp
export CORE_PEER_ADDRESS=peer0.org1.carmenw.com:7051
peer channel join -b /tmp/channel-artifacts/carmenchannel-3org.block export CORE_PEER_TLS_ENABLED=true
```
output should be looks like 

```
[020 06-01 03:42:07.18 UTC] [channelCmd] executeJoin -> INFO Successfully submitted proposal to join channel
```

**org1-peer1 joined to channel**  

```
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.carmenw.com/peers/peer1.org1.carmenw.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.carmenw.com/users/Admin@org1.carmenw.com/msp
export CORE_PEER_ADDRESS=peer1.org1.carmenw.com:8051
peer channel join -b /tmp/channel-artifacts/carmenchannel-3org.block
```

output should be looks like 

```
[01d 06-01 03:50:55.17 UTC] [channelCmd] InitCmdFactory -> INFO Endorser and orderer connections initialized
[01e 06-01 03:50:55.18 UTC] [msp.identity] Sign -> DEBU Sign: plaintext: 0AB3070A5B08011A0B089FC4DB940610...D52FCFD31A0A0A000A000A000A000A00 
[01f 06-01 03:50:55.18 UTC] [msp.identity] Sign -> DEBU Sign: digest: 157CC9F6A8B527166E2165891A4E15FB12AEDEA78F61122ACF0A2AFDC2B37A0A 
[020 06-01 03:50:55.57 UTC] [channelCmd] executeJoin -> INFO Successfully submitted proposal to join channel
```
**org2-peer0 joined to channel**  

```
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org2MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.carmenw.com/peers/peer0.org2.carmenw.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.carmenw.com/users/Admin@org2.carmenw.com/msp
export CORE_PEER_ADDRESS=peer0.org2.carmenw.com:9051
peer channel join -b /tmp/channel-artifacts/carmenchannel-3org.block
```

output should be looks like 

```
[01d 06-01 03:52:00.34 UTC] [channelCmd] InitCmdFactory -> INFO Endorser and orderer connections initialized
[01e 06-01 03:52:00.35 UTC] [msp.identity] Sign -> DEBU Sign: plaintext: 0AB4070A5C08011A0C08E0C4DB940610...D52FCFD31A0A0A000A000A000A000A00 
[01f 06-01 03:52:00.35 UTC] [msp.identity] Sign -> DEBU Sign: digest: 4227F105069B7D498982E2C7C4E31E47B829FE5179640C6358CC8A07E5B9363A 
[020 06-01 03:52:00.79 UTC] [channelCmd] executeJoin -> INFO Successfully submitted proposal to join channel
```
**org2-peer1 joined to channel**  

```
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org2MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.carmenw.com/peers/peer1.org2.carmenw.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.carmenw.com/users/Admin@org2.carmenw.com/msp
export CORE_PEER_ADDRESS=peer1.org2.carmenw.com:10051
peer channel join -b /tmp/channel-artifacts/carmenchannel-3org.block
```

output should be looks like 

```
[01d 06-01 03:52:36.81 UTC] [channelCmd] InitCmdFactory -> INFO Endorser and orderer connections initialized
[01e 06-01 03:52:36.82 UTC] [msp.identity] Sign -> DEBU Sign: plaintext: 0AB4070A5C08011A0C0884C5DB940610...D52FCFD31A0A0A000A000A000A000A00 
[01f 06-01 03:52:36.82 UTC] [msp.identity] Sign -> DEBU Sign: digest: 8180AB8D0D53D783A96FA31F9FBF24432F9A014E15EB61FE757F83624DD5A08F 
[020 06-01 03:52:37.22 UTC] [channelCmd] executeJoin -> INFO Successfully submitted proposal to join channel
```
**org3-peer0 joined to channel**  

```
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org3MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org3.carmenw.com/peers/peer0.org3.carmenw.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org3.carmenw.com/users/Admin@org3.carmenw.com/msp
export CORE_PEER_ADDRESS=peer0.org3.carmenw.com:11051
peer channel join -b /tmp/channel-artifacts/carmenchannel-3org.block
```

output should be looks like 

```
[01d 06-01 03:53:03.04 UTC] [channelCmd] InitCmdFactory -> INFO Endorser and orderer connections initialized
[01e 06-01 03:53:03.05 UTC] [msp.identity] Sign -> DEBU Sign: plaintext: 0AB3070A5B08011A0B089FC5DB940610...D52FCFD31A0A0A000A000A000A000A00 
[01f 06-01 03:53:03.05 UTC] [msp.identity] Sign -> DEBU Sign: digest: 8AD819555478DA9D177A70A56718F33BCFEE2C0F3B24B8D936C56BE1012EEF37 
[020 06-01 03:53:03.45 UTC] [channelCmd] executeJoin -> INFO Successfully submitted proposal to join channel
```
**org3-peer1 joined to channel**  

```
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org3MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org3.carmenw.com/peers/peer1.org3.carmenw.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org3.carmenw.com/users/Admin@org3.carmenw.com/msp
export CORE_PEER_ADDRESS=peer1.org3.carmenw.com:12051
peer channel join -b /tmp/channel-artifacts/carmenchannel-3org.block
```

output should be looks like 

```
[01d 06-01 03:54:13.31 UTC] [channelCmd] InitCmdFactory -> INFO Endorser and orderer connections initialized
[01e 06-01 03:54:13.32 UTC] [msp.identity] Sign -> DEBU Sign: plaintext: 0AB4070A5C08011A0C08E5C5DB940610...D52FCFD31A0A0A000A000A000A000A00 
[01f 06-01 03:54:13.32 UTC] [msp.identity] Sign -> DEBU Sign: digest: 70075520C46F68EFCA75D986A5DBEE58DB5AE06B6BF43693CB82ACF00EFB2945 
[020 06-01 03:54:13.72 UTC] [channelCmd] executeJoin -> INFO Successfully submitted proposal to join channel
```
## Updated Anchor peers
**Org1 Updated anchor peer**

```
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.carmenw.com/peers/peer0.org1.carmenw.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.carmenw.com/users/Admin@org1.carmenw.com/msp
export CORE_PEER_ADDRESS=peer0.org1.carmenw.com:7051
peer channel update -o orderer.carmenw.com:7050 -c carmenchannel-3org -f /tmp/channel-artifacts/Org1MSPanchors.tx --tls --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/carmenw.com/orderers/orderer.carmenw.com/msp/tlscacerts/tlsca.carmenw.com-cert.pem
``` 
Output:

```
[022 06-01 03:55:51.88 UTC] [channelCmd] update -> INFO Successfully submitted channel update
```

**Org2 Updated anchor peer**

```
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org2MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.carmenw.com/peers/peer0.org2.carmenw.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.carmenw.com/users/Admin@org2.carmenw.com/msp
export CORE_PEER_ADDRESS=peer0.org2.carmenw.com:9051
peer channel update -o orderer.carmenw.com:7050 -c carmenchannel-3org -f /tmp/channel-artifacts/Org2MSPanchors.tx --tls --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/carmenw.com/orderers/orderer.carmenw.com/msp/tlscacerts/tlsca.carmenw.com-cert.pem
```
Output:

```
[022 06-01 03:56:50.34 UTC] [channelCmd] update -> INFO Successfully submitted channel update
```

**Org3 Updated anchor peer**

```
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org3MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org3.carmenw.com/peers/peer0.org3.carmenw.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org3.carmenw.com/users/Admin@org3.carmenw.com/msp
export CORE_PEER_ADDRESS=peer0.org3.carmenw.com:11051
peer channel update -o orderer.carmenw.com:7050 -c carmenchannel-3org -f /tmp/channel-artifacts/Org3MSPanchors.tx --tls --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/carmenw.com/orderers/orderer.carmenw.com/msp/tlscacerts/tlsca.carmenw.com-cert.pem
```
Output:

```
[022 06-01 03:57:24.27 UTC] [channelCmd] update -> INFO Successfully submitted channel update
```

## Install Chaincode
This exection also under fabric-cli container 

**Go to chaincode directory and packed the contract** 

```
cd /etc/hyperledger/fabric/chaincodes/

peer lifecycle chaincode package java_demo.tar.gz --path /etc/hyperledger/fabric/chaincodes/FabricGeneralChainCode/ --lang java --label java_demo01
```
Output:

```
[007 06-01 04:04:10.79 UTC] [chaincode.platform.util] WriteFileToPackage -> DEBU Writing file to tarball: src/.DS_Store
[008 06-01 04:04:10.80 UTC] [chaincode.platform.util] WriteFileToPackage -> DEBU Writing file to tarball: src/chaincode.jar
```

### Install chaincode in different peers
**Org1-peer0 install chaincode**

```
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.carmenw.com/peers/peer0.org1.carmenw.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.carmenw.com/users/Admin@org1.carmenw.com/msp
export CORE_PEER_ADDRESS=peer0.org1.carmenw.com:7051
peer lifecycle chaincode install java_demo.tar.gz
``` 

Output:

```
[020 06-01 04:05:44.66 UTC] [cli.lifecycle.chaincode] submitInstallProposal -> INFO Installed remotely: response:<status:200 payload:"\nLjava_demo01:4786c83248869e90e137a60e2ba44822773b1c07624c46f35c067dae9c8204af\022\013java_demo01" > 
[021 06-01 04:05:44.66 UTC] [cli.lifecycle.chaincode] submitInstallProposal -> INFO Chaincode code package identifier: java_demo01:4786c83248869e90e137a60e2ba44822773b1c07624c46f35c067dae9c8204af
```

**Org1-peer1  install chaincode**

```
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.carmenw.com/peers/peer1.org1.carmenw.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.carmenw.com/users/Admin@org1.carmenw.com/msp
export CORE_PEER_ADDRESS=peer1.org1.carmenw.com:8051
peer lifecycle chaincode install java_demo.tar.gz
```

Output:

```
[020 06-01 04:07:32.69 UTC] [cli.lifecycle.chaincode] submitInstallProposal -> INFO Installed remotely: response:<status:200 payload:"\nLjava_demo01:4786c83248869e90e137a60e2ba44822773b1c07624c46f35c067dae9c8204af\022\013java_demo01" > 
[021 06-01 04:07:32.69 UTC] [cli.lifecycle.chaincode] submitInstallProposal -> INFO Chaincode code package identifier: java_demo01:4786c83248869e90e137a60e2ba44822773b1c07624c46f35c067dae9c8204af
```

**Org2-peer0  install chaincode**

```
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org2MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.carmenw.com/peers/peer0.org2.carmenw.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.carmenw.com/users/Admin@org2.carmenw.com/msp
export CORE_PEER_ADDRESS=peer0.org2.carmenw.com:9051
peer lifecycle chaincode install java_demo.tar.gz
```

Output:

```
[020 06-01 04:08:48.34 UTC] [cli.lifecycle.chaincode] submitInstallProposal -> INFO Installed remotely: response:<status:200 payload:"\nLjava_demo01:4786c83248869e90e137a60e2ba44822773b1c07624c46f35c067dae9c8204af\022\013java_demo01" > 
[021 06-01 04:08:48.34 UTC] [cli.lifecycle.chaincode] submitInstallProposal -> INFO Chaincode code package identifier: java_demo01:4786c83248869e90e137a60e2ba44822773b1c07624c46f35c067dae9c8204af
```

**Org2-peer1  install chaincode**

```
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org2MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.carmenw.com/peers/peer1.org2.carmenw.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.carmenw.com/users/Admin@org2.carmenw.com/msp
export CORE_PEER_ADDRESS=peer1.org2.carmenw.com:10051
peer lifecycle chaincode install java_demo.tar.gz
```

Output:

```
[020 06-01 04:10:47.66 UTC] [cli.lifecycle.chaincode] submitInstallProposal -> INFO Installed remotely: response:<status:200 payload:"\nLjava_demo01:4786c83248869e90e137a60e2ba44822773b1c07624c46f35c067dae9c8204af\022\013java_demo01" > 
[021 06-01 04:10:47.66 UTC] [cli.lifecycle.chaincode] submitInstallProposal -> INFO Chaincode code package identifier: java_demo01:4786c83248869e90e137a60e2ba44822773b1c07624c46f35c067dae9c8204af
```
**Org3-peer0  install chaincode**

```
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org3MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org3.carmenw.com/peers/peer0.org3.carmenw.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org3.carmenw.com/users/Admin@org3.carmenw.com/msp
export CORE_PEER_ADDRESS=peer0.org3.carmenw.com:11051
peer lifecycle chaincode install java_demo.tar.gz
```

Output:

```
[020 06-01 04:12:09.23 UTC] [cli.lifecycle.chaincode] submitInstallProposal -> INFO Installed remotely: response:<status:200 payload:"\nLjava_demo01:4786c83248869e90e137a60e2ba44822773b1c07624c46f35c067dae9c8204af\022\013java_demo01" > 
[021 06-01 04:12:09.23 UTC] [cli.lifecycle.chaincode] submitInstallProposal -> INFO Chaincode code package identifier: java_demo01:4786c83248869e90e137a60e2ba44822773b1c07624c46f35c067dae9c8204af
```

**Org3-peer1  install chaincode**

```
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org3MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org3.carmenw.com/peers/peer1.org3.carmenw.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org3.carmenw.com/users/Admin@org3.carmenw.com/msp
export CORE_PEER_ADDRESS=peer1.org3.carmenw.com:12051
peer lifecycle chaincode install java_demo.tar.gz
```

Output:

```
[020 06-01 04:13:18.90 UTC] [cli.lifecycle.chaincode] submitInstallProposal -> INFO Installed remotely: response:<status:200 payload:"\nLjava_demo01:4786c83248869e90e137a60e2ba44822773b1c07624c46f35c067dae9c8204af\022\013java_demo01" > 
[021 06-01 04:13:18.90 UTC] [cli.lifecycle.chaincode] submitInstallProposal -> INFO Chaincode code package identifier: java_demo01:4786c83248869e90e137a60e2ba44822773b1c07624c46f35c067dae9c8204af
```
**Query package ID:**          
Same package ID for every peer

```
peer lifecycle chaincode queryinstalled

```
Output:

```
Installed chaincodes on peer:
Package ID: java_demo01:4786c83248869e90e137a60e2ba44822773b1c07624c46f35c067dae9c8204af, Label: java_demo01
```

**Save this PACKAGE ID as environment variable**

```
export CC_PACKAGE_ID=java_demo01:4786c83248869e90e137a60e2ba44822773b1c07624c46f35c067dae9c8204af
```

## Approved the defined chaincode
**Org1 approved chaincode**

```
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.carmenw.com/peers/peer0.org1.carmenw.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.carmenw.com/users/Admin@org1.carmenw.com/msp
export CORE_PEER_ADDRESS=peer0.org1.carmenw.com:7051
peer lifecycle chaincode approveformyorg -o orderer.carmenw.com:7050 --ordererTLSHostnameOverride orderer.carmenw.com --channelID carmenchannel-3org --name PaperSystem  --version 1.0 --package-id $CC_PACKAGE_ID --sequence 1 --tls --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/carmenw.com/orderers/orderer.carmenw.com/msp/tlscacerts/tlsca.carmenw.com-cert.pem
```

Output:

```
[025 06-01 04:19:12.22 UTC] [chaincodeCmd] ClientWait -> INFO txid [621b20ecaefe1f624795ce653489b240da5ef9f94a65f40976fb88ce4aede6a1] committed with status (VALID) at peer0.org1.carmenw.com:7051
```

**Org2 approved chaincode**

```
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org2MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.carmenw.com/peers/peer0.org2.carmenw.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.carmenw.com/users/Admin@org2.carmenw.com/msp
export CORE_PEER_ADDRESS=peer0.org2.carmenw.com:9051
peer lifecycle chaincode approveformyorg -o orderer.carmenw.com:7050 --ordererTLSHostnameOverride orderer.carmenw.com --channelID carmenchannel-3org --name PaperSystem  --version 1.0 --package-id $CC_PACKAGE_ID --sequence 1 --tls --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/carmenw.com/orderers/orderer.carmenw.com/msp/tlscacerts/tlsca.carmenw.com-cert.pem
```

Output:

```
[025 06-01 04:20:49.85 UTC] [chaincodeCmd] ClientWait -> INFO txid [be31965b3472e5f6320f1dbc710d9f15495586979024ed9063dd606307bfff5e] committed with status (VALID) at peer0.org2.carmenw.com:9051
```

**Org3 approved chaincode**

```
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org3MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org3.carmenw.com/peers/peer0.org3.carmenw.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org3.carmenw.com/users/Admin@org3.carmenw.com/msp
export CORE_PEER_ADDRESS=peer0.org3.carmenw.com:11051
peer lifecycle chaincode approveformyorg -o orderer.carmenw.com:7050 --ordererTLSHostnameOverride orderer.carmenw.com --channelID carmenchannel-3org --name PaperSystem  --version 1.0 --package-id $CC_PACKAGE_ID --sequence 1 --tls --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/carmenw.com/orderers/orderer.carmenw.com/msp/tlscacerts/tlsca.carmenw.com-cert.pem
```

Output:

```
[025 06-01 04:21:16.99 UTC] [chaincodeCmd] ClientWait -> INFO txid [31abf70863cbe383be6225e977b310da500b2f550056ce7abf4987c7c53f7b40] committed with status (VALID) at peer0.org3.carmenw.com:11051
```
**Check whether all channel members have approved the same chaincode:**

```
peer lifecycle chaincode checkcommitreadiness --channelID carmenchannel-3org --name PaperSystem --version 1.0 --sequence 1 --tls --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/carmenw.com/orderers/orderer.carmenw.com/msp/tlscacerts/tlsca.carmenw.com-cert.pem --output json
```
Output:

```
{
	"approvals": {
		"Org1MSP": true,
		"Org2MSP": true,
		"Org3MSP": true
	}
}
```

**Summit Chaincode to Channel**

```
peer lifecycle chaincode commit -o orderer.carmenw.com:7050 --ordererTLSHostnameOverride orderer.carmenw.com --channelID carmenchannel-3org --name PaperSystem --version 1.0 --sequence 1 --tls --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/carmenw.com/orderers/orderer.carmenw.com/msp/tlscacerts/tlsca.carmenw.com-cert.pem --peerAddresses peer0.org1.carmenw.com:7051 --tlsRootCertFiles /etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.carmenw.com/peers/peer0.org1.carmenw.com/tls/ca.crt --peerAddresses peer0.org2.carmenw.com:9051 --tlsRootCertFiles /etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.carmenw.com/peers/peer0.org2.carmenw.com/tls/ca.crt --peerAddresses peer0.org3.carmenw.com:11051 --tlsRootCertFiles /etc/hyperledger/fabric/crypto-config/peerOrganizations/org3.carmenw.com/peers/peer0.org3.carmenw.com/tls/ca.crt
```

Output:

```
[02d 06-01 04:24:49.34 UTC] [chaincodeCmd] ClientWait -> INFO txid [3e02f1df6f637a2444535f970c0554cba06ac112d672230fc7c6b03d71b173c9] committed with status (VALID) at peer0.org3.carmenw.com:11051
[02e 06-01 04:24:50.63 UTC] [chaincodeCmd] ClientWait -> INFO txid [3e02f1df6f637a2444535f970c0554cba06ac112d672230fc7c6b03d71b173c9] committed with status (VALID) at peer0.org2.carmenw.com:9051
[02f 06-01 04:24:52.11 UTC] [chaincodeCmd] ClientWait -> INFO txid [3e02f1df6f637a2444535f970c0554cba06ac112d672230fc7c6b03d71b173c9] committed with status (VALID) at peer0.org1.carmenw.com:7051
```

**Check Whether Chaincode Already summit to Channel**

```
peer lifecycle chaincode querycommitted --channelID carmenchannel-3org --name PaperSystem --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/carmenw.com/orderers/orderer.carmenw.com/msp/tlscacerts/tlsca.carmenw.com-cert.pem
```

Output:

```
Committed chaincode definition for chaincode 'PaperSystem' on channel 'carmenchannel-3org':
Version: 1.0, Sequence: 1, Endorsement Plugin: escc, Validation Plugin: vscc, Approvals: [Org1MSP: true, Org2MSP: true, Org3MSP: true]
```

## Invoke function
now we can invoke function which defined in our contract

You can login CouchDB to see the stored data        
  
* Go to http://localhost:5984/_utils      
* username: jwen758      
* password: carmenw     


**Created data**

```
peer chaincode invoke -o orderer.carmenw.com:7050 --ordererTLSHostnameOverride orderer.carmenw.com --tls --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/carmenw.com/orderers/orderer.carmenw.com/msp/tlscacerts/tlsca.carmenw.com-cert.pem -C carmenchannel-3org -n PaperSystem --peerAddresses peer0.org1.carmenw.com:7051 --tlsRootCertFiles /etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.carmenw.com/peers/peer0.org1.carmenw.com/tls/ca.crt --peerAddresses peer0.org2.carmenw.com:9051 --tlsRootCertFiles /etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.carmenw.com/peers/peer0.org2.carmenw.com/tls/ca.crt --peerAddresses peer0.org3.carmenw.com:11051 --tlsRootCertFiles /etc/hyperledger/fabric/crypto-config/peerOrganizations/org3.carmenw.com/peers/peer0.org3.carmenw.com/tls/ca.crt -c '{"function":"createData","Args":["test00","{\"ai\":true,\"arch\":true,\"dm\":true,\"edu\":false,\"inter\":false,\"net\":false,\"par\":false,\"secu\":false,\"ssy\":false,\"theo\":false,\"vr\":false,\"ReviewState\":true,\"PaperID\":\"none\",\"Credit\":1}"]}'
```

Output:

```
[028 06-01 04:31:02.41 UTC] [chaincodeCmd] chaincodeInvokeOrQuery -> INFO Chaincode invoke successful. result: status:200 payload:"{\"ai\":true,\"arch\":true,\"dm\":true,\"edu\":false,\"inter\":false,\"net\":false,\"par\":false,\"secu\":false,\"ssy\":false,\"theo\":false,\"vr\":false,\"ReviewState\":true,\"PaperID\":\"none\",\"Credit\":1}"
```
![](/Users/carmenw/Desktop/screenshot/25.png)

**Update Data**

```
peer chaincode invoke -o orderer.carmenw.com:7050 --ordererTLSHostnameOverride orderer.carmenw.com --tls --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/carmenw.com/orderers/orderer.carmenw.com/msp/tlscacerts/tlsca.carmenw.com-cert.pem -C carmenchannel-3org -n PaperSystem --peerAddresses peer0.org1.carmenw.com:7051 --tlsRootCertFiles /etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.carmenw.com/peers/peer0.org1.carmenw.com/tls/ca.crt --peerAddresses peer0.org2.carmenw.com:9051 --tlsRootCertFiles /etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.carmenw.com/peers/peer0.org2.carmenw.com/tls/ca.crt --peerAddresses peer0.org3.carmenw.com:11051 --tlsRootCertFiles /etc/hyperledger/fabric/crypto-config/peerOrganizations/org3.carmenw.com/peers/peer0.org3.carmenw.com/tls/ca.crt -c '{"function":"updateData","Args":["test00","{\"ai\":false,\"arch\":false,\"dm\":false,\"edu\":false,\"inter\":false,\"net\":false,\"par\":false,\"secu\":false,\"ssy\":true,\"theo\":true,\"vr\":true,\"ReviewState\":true,\"PaperID\":\"none\",\"Credit\":1}"]}'
```

Output:

```
[028 06-01 04:35:16.62 UTC] [chaincodeCmd] chaincodeInvokeOrQuery -> INFO Chaincode invoke successful. result: status:200 payload:"{\"ai\":false,\"arch\":false,\"dm\":false,\"edu\":false,\"inter\":false,\"net\":false,\"par\":false,\"secu\":false,\"ssy\":true,\"theo\":true,\"vr\":true,\"ReviewState\":true,\"PaperID\":\"none\",\"Credit\":1}" 
```
![](/Users/carmenw/Desktop/screenshot/26.png)

**Query Data**

```
peer chaincode invoke -o orderer.carmenw.com:7050 --ordererTLSHostnameOverride orderer.carmenw.com --tls --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/carmenw.com/orderers/orderer.carmenw.com/msp/tlscacerts/tlsca.carmenw.com-cert.pem -C carmenchannel-3org -n PaperSystem --peerAddresses peer0.org1.carmenw.com:7051 --tlsRootCertFiles /etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.carmenw.com/peers/peer0.org1.carmenw.com/tls/ca.crt --peerAddresses peer0.org2.carmenw.com:9051 --tlsRootCertFiles /etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.carmenw.com/peers/peer0.org2.carmenw.com/tls/ca.crt --peerAddresses peer0.org3.carmenw.com:11051 --tlsRootCertFiles /etc/hyperledger/fabric/crypto-config/peerOrganizations/org3.carmenw.com/peers/peer0.org3.carmenw.com/tls/ca.crt -c '{"function":"queryData","Args":["test00"]}'
```
Output:

```
[028 06-01 04:37:45.44 UTC] [chaincodeCmd] chaincodeInvokeOrQuery -> INFO Chaincode invoke successful. result: status:200 payload:"{\"ai\":false,\"arch\":false,\"dm\":false,\"edu\":false,\"inter\":false,\"net\":false,\"par\":false,\"secu\":false,\"ssy\":true,\"theo\":true,\"vr\":true,\"ReviewState\":true,\"PaperID\":\"none\",\"Credit\":1}" 
```

**Rich Query**

```
peer chaincode invoke -o orderer.carmenw.com:7050 --ordererTLSHostnameOverride orderer.carmenw.com --tls --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/carmenw.com/orderers/orderer.carmenw.com/msp/tlscacerts/tlsca.carmenw.com-cert.pem -C carmenchannel-3org -n PaperSystem --peerAddresses peer0.org1.carmenw.com:7051 --tlsRootCertFiles /etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.carmenw.com/peers/peer0.org1.carmenw.com/tls/ca.crt --peerAddresses peer0.org2.carmenw.com:9051 --tlsRootCertFiles /etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.carmenw.com/peers/peer0.org2.carmenw.com/tls/ca.crt --peerAddresses peer0.org3.carmenw.com:11051 --tlsRootCertFiles /etc/hyperledger/fabric/crypto-config/peerOrganizations/org3.carmenw.com/peers/peer0.org3.carmenw.com/tls/ca.crt -c '{"function":"richQuery","Args":["{\"selector\":{\"vr\":true,\"ReviewState\":true}, \"use_index\":[]}"]}'
```
Output:

```
[028 06-01 05:16:39.80 UTC] [chaincodeCmd] chaincodeInvokeOrQuery -> INFO Chaincode invoke successful. result: status:200 payload:"{\"resultList\":[{\"json\":\"{\\\"Credit\\\":1,\\\"PaperID\\\":\\\"none\\\",\\\"ReviewState\\\":true,\\\"ai\\\":false,\\\"arch\\\":false,\\\"dm\\\":false,\\\"edu\\\":false,\\\"inter\\\":false,\\\"net\\\":false,\\\"par\\\":false,\\\"secu\\\":false,\\\"ssy\\\":true,\\\"theo\\\":true,\\\"vr\\\":true}\",\"key\":\"test00\"}]}" 
```

**Delete Data**

```
peer chaincode invoke -o orderer.carmenw.com:7050 --ordererTLSHostnameOverride orderer.carmenw.com --tls --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/carmenw.com/orderers/orderer.carmenw.com/msp/tlscacerts/tlsca.carmenw.com-cert.pem -C carmenchannel-3org -n PaperSystem --peerAddresses peer0.org1.carmenw.com:7051 --tlsRootCertFiles /etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.carmenw.com/peers/peer0.org1.carmenw.com/tls/ca.crt --peerAddresses peer0.org2.carmenw.com:9051 --tlsRootCertFiles /etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.carmenw.com/peers/peer0.org2.carmenw.com/tls/ca.crt --peerAddresses peer0.org3.carmenw.com:11051 --tlsRootCertFiles /etc/hyperledger/fabric/crypto-config/peerOrganizations/org3.carmenw.com/peers/peer0.org3.carmenw.com/tls/ca.crt -c '{"function":"deleteData","Args":["test00"]}'
```
Output:

```
[028 06-01 05:30:11.50 UTC] [chaincodeCmd] chaincodeInvokeOrQuery -> INFO Chaincode invoke successful. result: status:200 payload:"{\"ai\":false,\"arch\":false,\"dm\":false,\"edu\":false,\"inter\":false,\"net\":false,\"par\":false,\"secu\":false,\"ssy\":true,\"theo\":true,\"vr\":true,\"ReviewState\":true,\"PaperID\":\"none\",\"Credit\":1}"
```

## Show Block & transcation On Hyperledger Explorer

Open another terminal page & the same directory under *CarmenNetwork-3Org*

* Entry *explorer* folder and start the yaml document

```
cd explorer

docker-compose -f docker-compose.yaml up -d
```
![](/Users/carmenw/Desktop/screenshot/27.png)

* Sucecessful created under docker 
![](/Users/carmenw/Desktop/screenshot/28.png)

* Open the browser and enter http://localhost:9090/     
 and  enter         
 username: jwen758     
 password: carmenw
![](/Users/carmenw/Desktop/screenshot/29.png)

* Now we can see there are 6 peers were defined, including three Organisation and each Organisation has two peers.      
 & One Chaincode has been installed
![](/Users/carmenw/Desktop/screenshot/30.png)

# Shut down channel & testing network
the data only store if and only if the testing network is on.

* shut down yaml documents for channel under CarmenNetwork-3Org

```
docker-compose -f docker-compose-orderer-3org.yaml down -v
docker-compose -f docker-compose-org1-2peer.yaml down -v
docker-compose -f docker-compose-org2-2peer.yaml down -v
docker-compose -f docker-compose-org3-2peer.yaml down -v
```
* shut down yaml document for hyperledeger explorer under explorer

```
docker-compose -f docker-compose.yaml down -v
```





