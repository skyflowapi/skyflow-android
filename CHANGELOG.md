# Changelog

All notable changes to this project will be documented in this file.


## [1.18.0] - 2022-11-15
### Added
- `upsert` support while collecting data through skyflow elements.
- `upsert` support for pure js `insert` method.

## [1.17.0] - 2022-09-20

### Changed
- Removed `invokeConnection()`
- Removed `invokeSoapConnection()`

## [1.16.0] - 2022-06-21

### Changed
- Return BIN value for Card Number Collect Elements in prod env

## [1.15.0] - 2022-05-17

### Fixed
- add card icon for empty "CARD_NUMBER" collect element.

## [1.15.0] - 2022-05-10

### Added
- support for generic card numbers.

### Changed
- Deprecated `invokeConnection()`.
- Deprecated `invokeSoapConnection()`.

## [1.14.0] - 2022-04-19

### Added
- `EXPIRATION_YEAR` element type
- `EXPIRATION_MONTH` element type

## [1.13.0] - 2022-04-05

### Added
- support for application/x-www-form-urlencoded and multipart/form-data content-type's in connections.

## [1.12.1] - 2022-03-29

### Changed
- Added validation to token from TokenProvider

### Fixed 
-  requestHeaders are not case insensitive

## [1.12.0] - 2022-02-24

### Added
- `requestId` in error logs and error response for API errors

## [1.11.0] - 2022-02-08

### Added
- `replaceText` option for `RevealElement`

## [1.10.0] - 2022-01-25

### Added
- `formatRegex` option for `RevealElement`

## [1.9.2] - 2022-01-18

### Fixed
- Fixes in invokeSoapConnection method.

## [1.9.1] - 2022-01-11

### Fixed
- Fixed issues in responseXML for invokeSoapConnection method.

## [1.9.0] - 2022-01-04

### Added
- `Soap protocol` support for connections

## [1.8.0] - 2021-12-07

### Added
- `setError(error : String)` method to set custom UI error to be displayed on the collect and reveal Elements
- `resetError()` method is used to clear the custom UI error message set through setError 
- `format` parameter in collectElementOptions to support different type of date formats for EXPIRATION_DATE element
- `setValue(value: String)` and `clearValue()` method in DEV env, to set/clear the value of a collect element.
- `setToken(value: String)` method to set the token for a reveal element.
- `setAltText(value: String)` and `clearAltText()` method to set/clear the altText for a reveal 

### Changed
- Changed error messages in the logs and callback errors.
- altText support has been deprecated for collect element
- vaultId and vaultURL are now optional parameters in Configuration constructor

### Fixed
- Updating UI error messages

## [1.7.0] - 2021-11-24

### Added
- `validations` option in `CollectElementInput` that takes a set of validation rules
- `RegexMatchRule`, `LengthMatchRule` & `ElementValueMatchRule` Validation rules
- `PIN` element type

### Fixed
- Card Number validation


## [1.6.0] - 2021-11-17

### Added
- `enableCardIcon` option to configure Card Icon visibility
- `INPUT_FIELD` Element type for custom UI elements
- `unmount` method to reset collect element

### Changed
- New VISA Card Icon with updated Logo

### Fixed
- Added column name and table name in duplicate fields error message.
- Made token mandatory for reveal element while using invokeConnection, if reveal element is used in request body, path or query parameters.

## [1.5.0] - 2021-11-10

### Changed
- Renamed `invokeGateway` to `invokeConnection`
- Renamed `gatewayURL` to `connectionURL`
- Renamed `GatewayConfiguration` to `ConnectionConfig`

## [1.4.0] - 2021-10-26

### Added

Detecting card type and displaying icon in the card number element

## [1.3.0] - 2021-10-19

### Added

- `logLevel` option to allow different levels of logging
- event listeners for collect element
- `env` option for accessibilty of value in event listners

### Changed
- Standardized error information for easier debugging.
- deprecated redaction in `detokenize` method and `revealElementInput` initializer.
- change in `detokenize` response format.

## [1.2.0] - 2021-10-05

### Added

- invokeGateway method to work with inbound/outbound integrations using Skyflow Gateway

### Changed
- `table` and `column` are optional in CollectElementInput, when using invokeGateway
- `token` and `redaction` are optional in RevealElementInput, when using invokeGateway



## [1.1.0] - 2021-09-22

### Added

- getById method to reveal fields using SkyflowID's

- support for Non-PCI fields, data can be passed as additional fields in `CollectOptions` of container.collect method.
- `altText` for CollectElement 
- `labelStyles` for CollectElement
- `errorTextStyles` for CollectElement

- `altText` for RevealElement 
- `labelStyles` for RevealElement
- `errorTextStyles` for RevealElement
- default error message for reveal element

### Changed

- Moved from jitpack registry to github packages.
- Renamed `styles` to `inputStyles` in CollectElementInput and RevealElementInput.
- Renamed `get` method to `detokenize`.
- Renamed `id` to `token` in request and response of `detokenize` and `container.reveal()`.
- Changed `InsertOptions` to `CollectOptions` in collect method of container.


### Fixed
- Fixed issues in styling.
- Fixed timeout issue when url is not secure.
