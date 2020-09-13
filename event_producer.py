import functools
import random
import datetime
import uuid
import argparse
import json

from kafka import KafkaProducer
from faker import Faker


fake = Faker()


def random_browser() -> str:
    """
    Return a random web browser.
    """
    browsers = ["Chrome", "Firefox", "Safari"]
    return random.choice(browsers)


def message_envelope(event_type):
    """
    Decorator function that wraps raw event data
    in a message envelope.

    Parameters:
    event_type (str): the inner event type
    """

    def decorator_with_type(func):
        @functools.wraps(func)
        def wrapper():
            event = func()
            message_envelope = {
                "id": str(uuid.uuid4()),
                "event_type": event_type,
                "timestamp": str(datetime.datetime.now()),
                "event_body": event,
            }
            return message_envelope

        return wrapper

    return decorator_with_type


def create_common_event_attrs():
    """
    Generate common event attributes.
    """
    return {
        "user_agent": fake.user_agent(),
        "session_id": fake.uuid4(),
        "url": fake.url(),
        "browser": random_browser(),
        "ip": fake.ipv4(),
    }


@message_envelope("page_viewed.v1")
def create_pageview_event():
    """
    Generate a random page viewed event.
    """
    event = {"common": create_common_event_attrs()}

    return event


@message_envelope("link_clicked.v1")
def create_link_clicked_event():
    """
    Generate a random link clicked event.
    """
    event = {"common": create_common_event_attrs(), "link_url": fake.url()}

    return event


def create_events(num_events):
    """
    Generate the specified number of random events.

    Parameters:
    num_events (int): number of random events to generate
    """
    page_view_events = [create_pageview_event() for i in range(num_events)]
    link_clicked_events = [create_link_clicked_event() for i in range(num_events)]

    return page_view_events + link_clicked_events


def send_events_to_kafka(events, topic, brokers):
    """
    Send a list of events to the specified Kafka topic.

    Parameters:
    events  (list): events to send to Kafka
    topic   (str):  Kafka topic to send events to
    brokers (str):  Kafka brokers
    """
    producer = KafkaProducer(
        bootstrap_servers=brokers,
        value_serializer=lambda v: json.dumps(v).encode("utf-8"),
    )

    for event in events:
        future = producer.send(topic, event)
        res = future.get(timeout=60)
        print(res)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()

    parser.add_argument(
        "--num_events",
        type=int,
        required=True,
        help="number of events to produce"
    )

    parser.add_argument(
        "--topic",
        type=str,
        required=True,
        help="Kafka topic"
    )

    parser.add_argument(
        "--brokers",
        type=str,
        required=True,
        help="Kafka broker list"
    )

    args = parser.parse_args()

    events = create_events(args.num_events)
    send_events_to_kafka(events, args.topic, args.brokers)
