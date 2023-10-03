from libs.get_data_from_ng import get_tickets_status
from libs.en_logger import Logger
import logging
from libs.mongo_connect import set_up_mongo
from libs.atlassian_rest_api_calls import query_jira_status
from libs.update_ng_data import update_en_data_ticket_status, update_en_data_storm_key_status
from libs.read_config import configuration
from pathlib import Path
import datetime


def update_log_handler():

    log_config = configuration["LOG"]

    log_dir = log_config['log_file_path']

    try:
        Path(log_dir).mkdir(parents=True, exist_ok=False)
    except FileExistsError:
        pass

    log_date = (str(datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')).replace(" ", "")).replace(":", "")
    log_file = log_dir + "/log_status_update_" + log_date + ".log"
    handler = logging.FileHandler(log_file)
    formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
    handler.setFormatter(formatter)
    Logger.logger.setLevel(getattr(logging, log_config['log_level'].upper()))
    Logger.logger.addHandler(handler)

    Logger.logger.info(f'Logs will be written to file {log_file} with log level {log_config["log_level"].upper()}.')


if __name__ == '__main__':

    update_log_handler()

    Logger.logger.info("Processing started for inactivating EN workItemIds")

    db = set_up_mongo()

    tickets_dic = get_tickets_status(db)

    for key, row in tickets_dic.items():
        reply = query_jira_status(key)
        status = reply.upper()
        if status == "CLOSED":
            rdu_status = "I"
        else:
            rdu_status = "A"

        if rdu_status == "I":
            update_en_data_ticket_status(db, key, rdu_status)
            update_en_data_storm_key_status(db, row["storm_key"], rdu_status)
            Logger.logger.info(f"Inactivated workItemId: {key}, with stormKey: {row['storm_key']}")


    Logger.logger.info("Run for inactivating EN workItemIds completed succesfully.")
