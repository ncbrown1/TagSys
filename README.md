TagSys
======

Android App that connects to your LDAP server and custom REST API. Utilizes NFC to "check in" wherever you place an NFC tag. Classes include Location, Device, Tag, and Check. A location is a specific place/building in which tags can be located. A device can be anything from a printer to a chair to a paper closet. A tag is a specific NFC tag that is added to the REST API through the app, which collects the tag's uuid and matches it with a specific location, device type, and description. A check happens when in the Check Activity and scan an NFC tag; the scan registers the tag's uuid and matches it to a specific tag in the database, and if it is valid, you can give a certain status and short description of the tag's current state.
