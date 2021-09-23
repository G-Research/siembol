# How to set-up a netflow version 9 parser
This how-to guide outlines the procedure to get netflow version 9 parsing in siembol. 

A useful reference guide for netflow version 9 can be found in this cisco whitepaper.

https://www.cisco.com/en/US/technologies/tk648/tk362/technologies_white_paper09186a00800a3db9.html

## Collect netflow v9 from network devices
This guide assumes that your environment is running network infrastructure that produce netflow data (version 9). How to enable this from your network infrastructure is beyond the scope of this document, but your network infrastructure team can usually enable this directly from network switches, or forward it from a netflow collection aggregator. 

## Limitations of netflow v9 protocol
### Non-unique Source ID in netflow headers
The reason the siembol netflow parser requires the source IP from the original device to key templates with is because of a limitation in the netflow version 9 protocol export mechanism. As the whitepaper mentions above the netflow records should contain a header with a Source ID unique to every network device:

```
The Source ID field is a 32-bit value that is used to guarantee uniqueness for all flows exported from a particular device. (The Source ID field is the equivalent of the engine type and engine ID fields found in the NetFlow Version 5 and Version 8 headers). The format of this field is vendor specific. In the Cisco implementation, the first two bytes are reserved for future expansion, and will always be zero. Byte 3 provides uniqueness with respect to the routing engine on the exporting device. Byte 4 provides uniqueness with respect to the particular line card or Versatile Interface Processor on the exporting device. Collector devices should use the combination of the source IP address plus the Source ID field to associate an incoming NetFlow export packet with a unique instance of NetFlow on a particular device.
```

In many cases however netflow records will be exported with interface number's in the Source ID, which can be non-unique in an environment (usually 0 or 1), and presents a problem mapping templates to devices. For this reason the siembol parser uses the original source IP to manage this limitation. 

### Netflow flow records without a template
The netflow version 9 protocol uses an optimised binary message format that can be decoded by a siembol netflow parser only when the parser has received the beaconed template from a switch or router. For a period up until the template is received netflow packets will be discarded by the siembol netflow parser.

## Use a key to identify a device source in a kafka message
The siembol netflow parser requires a kafka key that maps the templates and flow records to each other. The simplest way to achieve this is to record the UDP source IP address and set the kafka key to that IP address. The kafka message should be the unmodified netflow UDP payload. 

## Create a netflow parser
After verifying that kafka events are being published with the correct key and message, setting up the parser in siembol should be trivial. 

Navigate to the parser configuration service, create a new parser and set the parser type to `netflow`. No other settings are be required. 

After the parser configuration is set, create a new parser application of type `single parser`, and link it to the netflow parser configuration. It should now start decoding the netflow kafka stream. 
