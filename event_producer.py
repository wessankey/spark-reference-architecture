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


def event_wrapper(event_type):
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
            event_wrapper = {
                "eventId": str(uuid.uuid4()),
                "eventType": event_type,
                "eventTimestamp": str(datetime.datetime.now()),
                "event": event,
            }
            return event_wrapper

        return wrapper

    return decorator_with_type


def create_common_event_attrs() -> dict:
    """
    Generate common event attributes.
    """
    return {
        "userAgent": fake.user_agent(),
        "sessionId": fake.uuid4(),
        "url": fake.url(),
        "browser": random_browser(),
        "ip": fake.ipv4(),
    }


@event_wrapper("page_viewed.v1")
def create_pageview_event() -> dict:
    """
    Generate a random page viewed event.
    """
    event = {"common": create_common_event_attrs()}

    return event


@event_wrapper("link_clicked.v1")
def create_link_clicked_event() -> dict:
    """
    Generate a random link clicked event.
    """
    event = {"common": create_common_event_attrs(), "link_url": fake.url()}

    return event


def create_events(num_events) -> list:
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
        required=False,
        default=10,
        help="number of events of each type to produce"
    )

    parser.add_argument(
        "--topic",
        type=str,
        required=False,
        default='raw_events',
        help="Kafka topic"
    )

    parser.add_argument(
        "--brokers",
        type=str,
        required=False,
        default='localhost:9092',
        help="Kafka broker list"
    )

    args = parser.parse_args()

    events = create_events(args.num_events)
    send_events_to_kafka(events, args.topic, args.brokers)
