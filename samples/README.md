# Skyflow Android SDK samples

Reference apps for the Skyflow Android SDKs. Each sample is a standalone app wired to its SDK module — pick the one that matches your vault:

| Sample | Vault type | Module | Guide |
|--------|-----------|--------|-------|
| [`skyvault/`](skyvault/) | PDB vault (v1 API) | `:skyvault` | [../skyvault/README.md](../skyvault/README.md) |
| [`flowvault/`](flowvault/) | Flow vault (v2 API) | `:flowvault` | [../flowvault/README.md](../flowvault/README.md) |

> These are **reference apps**: each declares a `project(':skyvault')` / `project(':flowvault')` dependency and is not part of the library CI build. To run one, open it in Android Studio and supply your vault details as described below.

Test a sample by adding your `VAULT_ID`, `VAULT_URL`, and bearer-token endpoint details in the required places.

## Prerequisites
- A Skyflow account. If you don't have one, register on the [Try Skyflow](https://skyflow.com/try-skyflow) page.
- [Node.js](https://nodejs.org/en/) version 10 or above (for the bearer-token endpoint below)
- [npm](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm) version 6.x.x
- [express.js](http://expressjs.com/en/starter/hello-world.html)
- Android Gradle Plugin 8.6.0 and above
- Android 5.0 (API level 21) and above

### Create the vault
1. In a browser, navigate to Skyflow Studio.
2. Create a vault by clicking **Create Vault** > **Upload Vault Schema**.
3. Choose the vault schema in the sample's `data/` folder (e.g. [skyvault/data/](skyvault/data/) or [flowvault/data/](flowvault/data/)).
4. Once the vault is created, click the gear icon and select **Edit Vault Details**.
5. Note your **Vault URL** and **Vault ID** values, then click **Cancel**. You'll need these later.

### Create a service account
1. In the side navigation, click **IAM** > **Service Accounts** > **New Service Account**.
2. For Name, enter "SDK Samples". For Roles, choose the roles required for the actions you'll test.
3. Click **Create**. Your browser downloads a **credentials.json** file. Keep it secure — you'll need it next.

### Create a service account bearer token generation endpoint
1. Create a new directory named `bearer-token-generator` and `cd` into it.
2. Run `npm init`, then install `skyflow-node` with `npm i skyflow-node`.
3. Move the downloaded `credentials.json` into this directory.
4. Create an `index.js` file with the following:
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
5. Start the server with `node index.js` — it listens at `localhost:3000`.

## Configure & run a sample
1. Open the sample (`skyvault/` or `flowvault/`) in Android Studio.
2. Add your values to the **root** `local.properties` file. The sample's `build.gradle` reads each key from `local.properties` and exposes it as a `BuildConfig` field, so the activities pick them up automatically — you never hand-edit the `.kt` files. Any key you leave unset falls back to a `<PLACEHOLDER>` default so the sample still compiles.
3. For reveal samples, also supply valid Skyflow IDs and data tokens — see [Get tokens for your stored data](https://docs.skyflow.com/tokenization-apis/#get-tokens-for-your-stored-data).
4. Build and run on a device or emulator (Android 5.0 / API 21+).

### FlowVault `local.properties` keys
The `flowvault/` sample reads the following keys (all optional — unset keys default to a placeholder):

| Key | Used by | Purpose |
|-----|---------|---------|
| `VAULT_ID` | all | Vault ID |
| `VAULT_URL` | all | Vault URL (host, no scheme) |
| `TOKEN_URL` | all | Bearer-token endpoint (e.g. `http://localhost:3000/`) used by `DemoTokenProvider` |
| `TABLE_NAME` | collect / update / card-brand | Table to insert or update into |
| `COLUMN_NAME` | collect / update / card-brand | Column for card number / name / cvv fields |
| `EXPIRY_COLUMN` | collect | Column for the expiration-date field |
| `COLUMN` | collect / update | Column key for an additional field |
| `VALUE` | collect / update | Value for an additional field |
| `UNIQUE_COLUMN` | collect (upsert) | Unique column for upsert |
| `SKYFLOW_ID` | update | Skyflow ID of the record to update |
| `TOKEN_1`, `TOKEN_2` | reveal | Data tokens to reveal |
| `TOKEN_GROUP_NAME` | reveal | Token group name for reveal-with-options |
| `REDACTION_TYPE` | reveal | Redaction name to apply (plain string, e.g. a schema redaction) |
| `BEARER_TOKEN` | (optional) | A literal bearer token. Not used by default — the card-brand demo fetches a fresh token via `TOKEN_URL`. Provided only if you prefer to hardcode one. |

For the SDK APIs each sample demonstrates, see the per-SDK guides linked in the table above.
