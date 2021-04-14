"""
Log generator script
Will produce a log line designed to demonstrate extractor chaining in Siembol. Log line will contain:
1. Syslog (RFC5424)
2. Json message
3. Key-value item inside a json element

Example:
 <165>1 2003-10-11T22:14:15.003Z mymachine.example.com
    evntslog - ID47 [exampleSDID@32473 iut="3" eventSource="Application" eventID="1011"]
    {'key': 'pid=0, ppid=1000, foo=bar'}

And should extract as follows:
{
  "eventID" : "1011",
  "syslog_msg_id" : "ID47",
  "syslog_appname" : "evntslog",
  "syslog_sd_id" : "exampleSDID@32473",
  "syslog_facility" : 20,
  "foo" : "bar",
  "eventSource" : "Application",
  "iut" : "3",
  "pid" : "0,",
  "source_type" : "example_log",
  "syslog_version" : 1,
  "syslog_severity" : 5,
  "ppid" : "1000,",
  "original_string" : "<165>1 2003-10-11T22:14:15.003Z mymachine.example.com evntslog - ID47 [exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"] {\"key\": \"pid=0, ppid=1000, foo=bar\"}",
  "syslog_hostname" : "mymachine.example.com",
  "syslog_msg" : "{\"key\": \"pid=0, ppid=1000, foo=bar\"}",
  "syslog_priority" : 165,
  "key" : "pid=0, ppid=1000, foo=bar",
  "timestamp" : 1065910455003
}

<165>1 2003-10-11T22:14:15.003000Z mymachine.example.com
           evntslog - ID47 [exampleSDID@32473 iut="3" eventSource=
           "Application" eventID="1011"] {'key': 'pid=0, ppid=1000'}
"""

import json

from datetime import datetime
from random import randint
from time import sleep
from kafka import KafkaProducer

ts = lambda: datetime.now().strftime("%Y-%m-%dT%H:%M:%S.%f")
host = lambda: ["host", "machine", "testing"][randint(0, 2)] + ".example.com"
json_object = lambda: json.dumps({"key": f"pid={randint(0, 50000)}, ppid={randint(0, 50000)}"})
generator = lambda: f"<165>1 {ts()} {host()} evntslog - ID47 [exampleSDID@32473 iut=\"{randint(0, 50)}\" " + \
                f"eventSource=\"Application\" eventID=\"{randint(0, 50000)}\"] {json_object()}"

producer = KafkaProducer(
   value_serializer=lambda m: m.encode('utf-8'), 
   bootstrap_servers="siembol-storm-kafka-headless.siembol.svc.cluster.local:9092")

while True:
  producer.send("siembol.parsing.syslog", value=generator())
  sleep(0.15)
