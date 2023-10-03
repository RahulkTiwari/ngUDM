from libraries.caches.config import config_object


class RunArguments(object):

    def __init__(self):
        self.mode = None
        self.date = "19700101 00:00:00"
        self.hours_offset = int(config_object['OFFSET_DEFAULT']['HOUR_OFFSET'])
        self.days_offset = int(config_object['OFFSET_DEFAULT']['DAY_OFFSET'])
