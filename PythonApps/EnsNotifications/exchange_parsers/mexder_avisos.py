# Third party libs
from requests_html import HTMLSession
from bs4 import BeautifulSoup
import re
from datetime import datetime
import time
# Custom libs
from ext_libs.googletrans import Translator
from modules.url_config import url_configuration
from objects.notice import Notice
from objects.url import Url
from modules.logger import main_logger

exchange = 'mexder_avisos'


def get_reference_id(href_string):
    url_obj = Url(href_string)
    ref_id = url_obj.document_root

    return ref_id


def get_subject(spanish_subject, exchange_reference_id):
    translator = Translator()
    try:
        en_subject = translator.translate(spanish_subject, src='es', dest='en')
        nicified_subject = re.sub('[\r\n]', '', en_subject.text)
    except TypeError:
        nicified_subject = 'Unable to translate subject'

    return nicified_subject


def get_notice_list():

    category_url_list = ['cete', 'bonos', 'divisas', 'ipc', 'udi', 'acciones', 'swap', 'commodities', 'FIBRAS', 'tiie', 'commodities_opciones',
                         'opc_acciones', 'opc_ipc', 'etfs', 'opc_divisas', 'MEX_FIBRAS', 'avisos_del_mes']

    session = HTMLSession()

    headers = {
        "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
    }
    root_url = url_configuration[exchange]['url']

    notice_list = []

    for each_url in category_url_list:
        url = f'{root_url}{each_url}'
        main_logger.debug(f'Processing MexDer url category: {each_url}')

        page = session.get(url, headers=headers)
        time.sleep(2)
        page_content = page.text

        soup = BeautifulSoup(page_content, 'lxml')
        notice_table_entries = soup.find_all('a', {'target': '_blank'})[0:20]

        notice_list.extend(notice_table_entries)

    return notice_list


def set_values(notice_table_row):
    notice = Notice(exchange)
    main_logger.debug(f'Setting values for {notice_table_row.text}')
    exchange_reference_id = get_reference_id(notice_table_row['href'])
    subject = get_subject(notice_table_row.text, exchange_reference_id)

    notice.exchangeReferenceId = exchange_reference_id
    notice.noticeSourceUniqueId = exchange_reference_id
    notice.eventInitialUrl = notice_table_row['href']
    notice.eventSubject = subject
    notice.eventPublishDate = datetime.now()
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
