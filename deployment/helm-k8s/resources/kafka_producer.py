from kafka import KafkaProducer
import os
import time
import json

topic = os.getenv('KAFKA_TOPIC')
message_key = os.getenv('MESSAGE_KEY')
frequency_per_second = int(os.getenv('MESSAGE_FREQUENCY_PER_SECOND'))
demo_messages_file = os.getenv('DEMO_MESSAGES_FILE')

with open(demo_messages_file, "r") as f:
    messages = json.load(f)
    
message = json.dumps(messages.get(message_key))
if message is None:
    raise Exception(f'Message not found in json: key {message_key} not found')

producer = KafkaProducer(bootstrap_servers=os.getenv('KAFKA_SERVERS'))
count = 0
while True:
    producer.send(topic, str.encode(message))
    time.sleep(1/frequency_per_second)
    count += 1
    if count%frequency_per_second == 0:
        print(f'Sent {count} messages to topic {topic}')