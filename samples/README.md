# Skyflow-Android Sdk Sample Templates
Use this folder to test the functionalities of JS-SDK just by adding `VAULT-ID` `VAULT-URL` and `SERVICE-ACCOUNT` details at the required place.


### Prerequisites
- [Node.js](https://nodejs.org/en/) version 10 or above
- [npm](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm) version 6.x.x
- [express.js](http://expressjs.com/en/starter/hello-world.html)
- Android Gradle plugin 4.2.0 and above
- Android 5.0 (API level 21) and above

## Configure
- Before you can run the sample app, create a vault.
- `TOKEN_URL` for generating bearer token.
- Add `include ':samples'` in [settings.gradle](../settings.gradle) file.
### Create The Vault
1. In a browser, navigate to Skyflow Studio and log in.
2. Create a vault by clicking **Create Vault** > **Upload Vault Schema**.
3. Choose [data/vaultSchema.json](data/vaultSchema.json).
4. Once the vault is created, click the gear icon and select **Edit Vault** Details.

### Create A Service Account
1. In the side navigation click, **IAM** > **Service Accounts** > **New Service Account**.
2. For Name, enter **Test-Js-Android-Sample**. For Roles, choose the required roles for specific action.
3. Click **Create**. Your browser downloads a **credentials.json** file. Keep this file secure, as you'll need it in the next steps.

### Create TOKEN_END_POINT_URL
- Create a new directory named `bearer-token-generator`.

        mkdir bearer-token-generator
- Navigate to `bearer-token-generator` directory.

        cd bearer-token-generator
- Initialize npm

        npm init
- Install `skyflow-node`

        npm i skyflow-node
- Create `index.js` file
- Open `index.js` file
- populate `index.js` file with below code snippet
```javascript
const express = require('express')
const app = express()
var cors = require('cors')
const port = 3000
const {
    generateBearerToken,
    isExpired
} = require('skyflow-node');

app.use(cors())

let filepath = 'cred.json';
let bearerToken = "";

function getSkyflowBearerToken() {
    return new Promise(async (resolve, reject) => {
        try {
            if (!isExpired(bearerToken)) resolve(bearerToken)
            else {
                let response = await generateBearerToken(filepath);
                bearerToken = response.accessToken;
                resolve(bearerToken);
            }
        } catch (e) {
            reject(e);
        }
    });
}

app.get('/', async (req, res) => {
  let bearerToken = await getSkyflowBearerToken();
  res.json({"accessToken" : bearerToken});
})

app.listen(port, () => {
  console.log(`Server is listening on port ${port}`)
})

```
- Start the server

        node index.js
    server will start at `localhost:3000`


## Sample Templates
- [`Collect data`](src/main/java/com/Skyflow/CollectActivity.kt)
   - This sample illustrates how to use secure Skyflow elements to collect sensitive user information and reveal it to the user via tokens.
   - Configure
        - Update `VAULT_ID` with the above created vault.
        - Update `VAULT_URL` with the above created vault.
        - Update `TOKEN_URL` with `http://localhost:3000/`
- [`Custom Validation`](src/main/java/com/Skyflow/CustomValidationActivity.kt)
    - This sample illustrates how to use custom validation with skylow collect elements.
    - Configure
        - Update `VAULT_ID` with the above created vault.
        - Update `VAULT_URL` with the above created vault.

- [`Reveal`](src/main/java/com/Skyflow/RevealActivity.kt)
    - This sample illustrates how functionality of reveal feature works. 
    - Configure
        - Update all `VAULT_ID` with the above created vault.
        - Update all `VAULT_URL` with the above created vault.
        - Update `<skyflow_id1>` and `<skyflow_id2>` with skyflow id. Skyflow id is a unique string attached with each row of data in Skyflow vault. You can get it from vault for existing row.
        - Update `<token1>` and `<token2>` with data tokens. Data tokens are the tokenized form of data. It can be accessed while insertion collection of data in console. Make sure that the tokens used here, exists in vault.


## Run Templates
- build and run in android       