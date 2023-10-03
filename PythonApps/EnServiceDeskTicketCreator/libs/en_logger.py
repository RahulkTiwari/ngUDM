import datetime
import logging


class Logger:
    log_date = (str(datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')).replace(" ", "")).replace(":", "")
    logger = logging.getLogger(__name__)
    logger.addHandler(logging.StreamHandler())
    logger.info("Stream logger has been initialized")

