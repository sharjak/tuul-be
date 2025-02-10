## Setup

To run integration tests, create a firestore emulator:

1. Install firebase-tools to set up emulator:
   ```sh
   npm install -g firebase-tools
   ```
2. Login with firebase
   ```sh
   firebase login
   ```
3. Init firebase emulators
   ```sh
   firebase init firebase
   ```
4. Start only firestore emulator
   ```sh
   firebase emulators:start --only firestore
   ```
5. Change the port to 8081 in firebase.json and restart the emulator
   ```json 
    {
      "emulators": {
        "singleProjectMode": true,
        "firestore": {
          "port": 8081
        }
      },
      "firestore": {
         "rules": "firestore.rules",
         "indexes": "firestore.indexes.json"
      }
    }
   ```