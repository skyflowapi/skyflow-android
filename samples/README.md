# Android SDK samples
Test the SDK by adding `VAULT-ID`, `VAULT-URL`, and `SERVICE-ACCOUNT` details in the required places for each sample.


## Prerequisites
- A Skylow account. If you don't have one, register for one on the [Try Skyflow](https://skyflow.com/try-skyflow) page.
- [Node.js](https://nodejs.org/en/) version 10 or above
- [npm](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm) version 6.x.x
- [express.js](http://expressjs.com/en/starter/hello-world.html)
- Android Gradle plugin 4.2.0 and above
- Android 5.0 (API level 21) and above

## Prepare
- Add `include ':samples'` in [settings.gradle](../settings.gradle) file.

### Create the vault
1. In a browser, navigate to Skyflow Studio.
2. Create a vault by clicking **Create Vault** > **Upload Vault Schema**.
3. Choose [data/vaultSchema.json](data/vaultSchema.json).
4. Once the vault is created, click the gear icon and select **Edit Vault Details**.
5. Note your **Vault URL** and **Vault ID** values, then click **Cancel**. You'll need these later.
### Create a service account
1. In the side navigation click, **IAM** > **Service Accounts** > **New Service Account**.
2. For Name, enter "SDK Samples". For Roles, choose the required roles for specific action.
3. Click **Create**. Your browser downloads a **credentials.json** file. Keep this file secure, as you'll need it in the next steps.

### Create a service account bearer token generation endpoint
1. Create a new directory named `bearer-token-generator`.

        mkdir bearer-token-generator
2. Navigate to `bearer-token-generator` directory.

        cd bearer-token-generator
3. Initialize npm

        npm init
4. Install `skyflow-node`

        npm i skyflow-node
5. Move the downloaded “credentials.json” file generated from [Create a service account](#create-a-service-account) step into the `bearer-token-generator` directory.        
6. Create `index.js` file
7. Open `index.js` file
8. Populate `index.js` file with below code snippet
```javascript
const express = require("express");
const app = express();
const cors = require("cors");
const port = 3000;
const {
   generateBearerToken,
   isExpired
} = require("skyflow-node");

app.use(cors());

let filepath = "credentials.json";
let bearerToken = "";

const getSkyflowBearerToken = () => {
   return new Promise(async (resolve, reject) => {
       try {
           if (!isExpired(bearerToken)) {
               resolve(bearerToken);
           }
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

app.get("/", async (req, res) => {
 let bearerToken = await getSkyflowBearerToken();
 res.json({"accessToken" : bearerToken});
});

app.listen(port, () => {
 console.log(`Server is listening on port ${port}`);
})

```
9. Start the server

        node index.js
    server will start at `localhost:3000`


## The samples
### Skyflow Elements
This sample demonstrates how to use Skyflow Elements to collect sensitive user information and reveal it to a user.
#### Configure
1. Update `VAULT_ID`.
2. Update `VAULT_URL`.
3. Update `TOKEN_URL` with `http://localhost:3000/`

### Custom Validation
This sample demonstrates how to use custom validation with Skylow elements.
#### Configure
1. Update `VAULT_ID` with the above created vault.
2. Update `VAULT_URL` with the above created vault.

### Reveal
This sample demonstrates how to reveal sensitive data.
#### Configure
1. Update all `VAULT_ID`.
2. Update all `VAULT_URL`.
3. Update `<skyflow_id1>` and `<skyflow_id2>` with skyflow id. Skyflow id is a unique string attached with each row of data in Skyflow vault. You can get it from vault for existing row.
4. Update `<token1>` and `<token2>` with data tokens. Data tokens are the tokenized form of data. See [Get tokens for your stored data](https://docs.skyflow.com/tokenization-apis/#get-tokens-for-your-stored-data) to retrieve data tokens for your vault data.


## Run the samples
- Build and Run in Android Studio
