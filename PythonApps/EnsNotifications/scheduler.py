# Third party libs
import schedule
# Custom libs
from main import main
from modules.logger import main_logger

#schedule.every(1).hours.do(main)
# statrting at 12:00 IST
schedule.every().day.at("07:30").do(main)
schedule.every().day.at("10:30").do(main)
schedule.every().day.at("13:30").do(main)
schedule.every().day.at("16:30").do(main)
schedule.every().day.at("19:30").do(main)
schedule.every().day.at("22:30").do(main)
schedule.every().day.at("01:30").do(main)
schedule.every().day.at("04:30").do(main)

# main_logger.info('Starting intial run before scheduler')
# main()
while True:
    schedule.run_pending()
