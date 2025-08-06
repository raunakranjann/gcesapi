

-----

# GCES Third Party API Documentation

This documentation provides details on the API endpoints for the GCES integration, including authentication and data synchronization for different administrative boundaries.

**Base URL**: `http://localhost:8080/gces`

-----

## User Authentication

This endpoint is used to authenticate a user and generate an access token, which is stored in the database for subsequent API calls.

### `POST /login`

  * **Description**: Authenticates a user with a username and password, then stores the resulting access token in the `user_tokens` table.
  * **Method**: `POST`
  * **Request Body**: `application/json`
    ```json
    {
      "isFarmerGrievance": false,
      "userName": "XXXXXXXX82",
      "userPassword": "00XXXX"
    }
    ```
  * **Success Response (200 OK)**:
    ```
    Login successful. Token stored for user: XXXXXXXX82
    ```
  * **Error Response (400 Bad Request)**:
    ```
    Error: Authentication failed: 400 - Invalid credentials.
    ```
  * **Postman Test Case**:
    1.  Set the Method to `POST`.
    2.  Set the URL to `http://localhost:8080/gces/login`.
    3.  Go to the `Body` tab, select `raw`, and choose `JSON`.
    4.  Paste the request body JSON provided above and click `Send`.

-----


## State Data Synchronization

### `POST /sync-state`

  * **Description**: Synchronizes state data using an existing token. Assumes a token exists for the given user.
  * **Method**: `POST`
  * **Request Body**: `application/json`
    ```json
    {
      "userName": "XXXXXXXX82"
    }
    ```
  * **Success Response (200 OK)**:
    ```
    X states synchronized for user: XXXXXXXX82
    ```
  * **Error Response (400 Bad Request)**:
    ```
    Error: No token found for user: XXXXXXXX82. Please authenticate first.
    ```
  * **Postman Test Case**:
    1.  First, ensure you have successfully called `POST /login`.
    2.  Set the Method to `POST`.
    3.  Set the URL to `http://localhost:8080/gces/sync-state`.
    4.  Go to the `Body` tab, select `raw`, and choose `JSON`.
    5.  Paste the request body JSON provided above and click `Send`.

-----

## District Data Synchronization

### `POST /sync-district`

  * **Description**: Synchronizes district data using an existing token. Assumes a token exists for the given user.
  * **Method**: `POST`
  * **Request Body**: `application/json`
    ```json
    {
      "userName": "XXXXXXXX82"
    }
    ```
  * **Success Response (200 OK)**:
    ```
    X districts synchronized for user: XXXXXXXX82
    ```
  * **Error Response (400 Bad Request)**:
    ```
    Error: No token found for user: XXXXXXXX82. Please authenticate first.
    ```
  * **Postman Test Case**:
    1.  First, ensure you have successfully called `POST /login`.
    2.  Set the Method to `POST`.
    3.  Set the URL to `http://localhost:8080/gces/sync-district`.
    4.  Go to the `Body` tab, select `raw`, and choose `JSON`.
    5.  Paste the request body JSON provided above and click `Send`.

-----

## Sub-District Data Synchronization

### `POST /sync-subdistrict`

  * **Description**: Synchronizes district data using an existing token. Assumes a token exists for the given user.
  * **Method**: `POST`
  * **Request Body**: `application/json`
    ```json
    {
      "userName": "XXXXXXXX82"
    }
    ```
  * **Success Response (200 OK)**:
    ```
    X sub-districts synchronized for user: XXXXXXXX82
    ```
  * **Error Response (400 Bad Request)**:
    ```
    Error: No token found for user: XXXXXXXX82. Please authenticate first.
    ```
  * **Postman Test Case**:
    1.  First, ensure you have successfully called `POST /login`.
    2.  Set the Method to `POST`.
    3.  Set the URL to `http://localhost:8080/gces/sync-subdistrict`.
    4.  Go to the `Body` tab, select `raw`, and choose `JSON`.
    5.  Paste the request body JSON provided above and click `Send`.

-----






## Village Data Synchronization (Before syncing villages, it is essential to first sync the sub-districts.)

These endpoints are used to synchronize village data from the external API.


### `POST /sync-villages-lgd`

  * **Description**: Synchronizes village data using an existing token for the specified user. This endpoint assumes the user has already logged in. It accepts state LGD codes.
  * **Method**: `POST`
  * **Request Body**: `application/json`
      * **Without LGD codes (uses defaults from the service)**:
        ```json
        {
          "userName": "XXXXXXXX82",
           "stateLGDCodeList": [7] //the state lgd-code for delhi is '7'
        }
        ```
  * **Success Response (200 OK)**:
    ```
    105 villages synchronized for user: XXXXXXXX82
    ```
  * **Error Response (400 Bad Request)**:
    ```
    Error: No token found for user: XXXXXXXX82. Please authenticate first.
    ```
  * **Postman Test Case**:
    1.  First, ensure you have successfully called `POST /login`.
    2.  Set the Method to `POST`.
    3.  Set the URL to `http://localhost:8080/gces/sync-villages-lgd`.
    4.  Go to the `Body` tab, select `raw`, and choose `JSON`.
    5.  Paste the request body JSON provided above and click `Send`.

-----


### `POST /login-and-sync-villages`

  * **Description**: Performs a full login and then synchronizes village data. This endpoint is useful for a first-time login where a token might not exist. It accepts state LGD codes to filter the villages.
  * **Method**: `POST`
  * **Request Body**: `application/json`
      * **With optional LGD codes**:
        ```json
        {
          "isFarmerGrievance": false,
          "userName": "XXXXXXXX82",
          "userPassword": "00XXXX",
          "stateLGDCodeList": [10] //the state lgd-code for bihar is '10'
        }
        ```
 
  * **Success Response (200 OK)**:
    ```
    Authentication successful and 105 villages synchronized for user: XXXXXXXX82
    ```
  * **Postman Test Case**:
    1.  Set the Method to `POST`.
    2.  Set the URL to `http://localhost:8080/gces/login-and-sync-villages`.
    3.  Go to the `Body` tab, select `raw`, and choose `JSON`.
    4.  Paste one of the request bodies above and click `Send`.





## Token Retrieval (Verification)

### `GET /token/{userName}`

  * **Description**: Retrieves the stored token details for a specific user.
  * **Method**: `GET`
  * **URL**: `http://localhost:8080/gces/token/XXXXXXXX82`
  * **Success Response (200 OK)**:
    ```json
    {
      "id": 1,
      "userName": "XXXXXXXX82",
      "token": "your_long_generated_token_string",
      "userId": XXXX41,
      "createdAt": "2025-08-03T18:00:00"
    }
    ```
  * **Error Response (404 Not Found)**:
    ```
    No Content
    ```
  * **Postman Test Case**:
    1.  Set the Method to `GET`.
    2.  Set the URL to `http://localhost:8080/gces/token/XXXXXXXX82`.
    3.  Click `Send`.

-----
