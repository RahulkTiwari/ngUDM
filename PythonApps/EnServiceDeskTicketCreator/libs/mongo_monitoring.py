from pymongo import monitoring
from libs.en_logger import Logger


class CommandLogger(monitoring.CommandListener):

    def started(self, event):
        Logger.logger.debug("Command {0.command_name} with request id {0.request_id} started on server {0.connection_id}".format(event))

    def succeeded(self, event):
        Logger.logger.debug("Command {0.command_name} with request id {0.request_id} on server {0.connection_id} succeeded in {0.duration_micros} "
                            "microseconds".format(event))

    def failed(self, event):
        Logger.logger.info("Command {0.command_name} with request id {0.request_id} on server {0.connection_id} failed in {0.duration_micros} "
                           "microseconds".format(event))


monitoring.register(CommandLogger())


class ServerLogger(monitoring.ServerListener):

    def opened(self, event):
        Logger.logger.info("Server {0.server_address} added to topology {0.topology_id}".format(event))

    def description_changed(self, event):
        previous_server_type = event.previous_description.server_type
        new_server_type = event.new_description.server_type
        if new_server_type != previous_server_type:
            # server_type_name was added in PyMongo 3.4
            Logger.logger.info(
                "Server {0.server_address} changed type from {0.previous_description.server_type_name} to "
                "{0.new_description.server_type_name}".format(event))

    def closed(self, event):
        Logger.logger.warning("Server {0.server_address} removed from topology {0.topology_id}".format(event))


class HeartbeatLogger(monitoring.ServerHeartbeatListener):

    def started(self, event):
        Logger.logger.debug("Heartbeat sent to server {0.connection_id}".format(event))

    def succeeded(self, event):
        # The reply.document attribute was added in PyMongo 3.4.
        Logger.logger.debug("Heartbeat to server {0.connection_id} succeeded with reply {0.reply.document[localTime]}".format(event))

    def failed(self, event):
        Logger.logger.warning("Heartbeat to server {0.connection_id} failed with error {0.reply}".format(event))
