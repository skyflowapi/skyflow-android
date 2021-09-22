# Changelog

All notable changes to this project will be documented in this file.

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