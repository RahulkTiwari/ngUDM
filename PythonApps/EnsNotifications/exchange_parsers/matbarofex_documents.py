# Third party libs
from requests_html import HTMLSession
from bs4 import BeautifulSoup
import re
# Custom libs
from ext_libs.googletrans import Translator
from modules.generic_functions import convert_date
from modules.url_config import url_configuration
from objects.notice import Notice
from objects.url import Url

exchange = 'matbarofex_documents'


def prep_subject(subject_string):
    remove_regex = '^\s*\d{2}/\d{2}/\d{4}\s+-\s*'
    stripped_subject = re.sub(remove_regex, '', subject_string).strip().replace('\n', '')

    translator = Translator()
    try:
        en_subject = translator.translate(stripped_subject, src='es', dest='en').text
    except (AttributeError, TypeError):
        en_subject = 'Unable to translate subject'

    return en_subject


def get_exchange_reference_id(href_input):
    url = Url(href_input)
    sub_string = url.last_element
    ref_id = re.sub('\D', '', sub_string)

    return ref_id


def get_notice_list():
    session = HTMLSession()

    headers = {
        "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
    }
    url = url_configuration[exchange]['url']

    page = session.get(url, headers=headers, verify=False)

    page_content = page.text

    soup = BeautifulSoup(page_content, 'lxml')
    notice_table = soup.find('div', id='accordion')
    notice_table_entries = notice_table.find_all('div', class_='card')

    return notice_table_entries


def set_values(notice_table_row):
    notice = Notice(exchange)

    exchange_reference_id = get_exchange_reference_id(notice_table_row.find('div', class_='card-body').a['href'])
    english_subject = prep_subject(notice_table_row.find('div', class_='col col-md-11').text)

    notice.exchangeReferenceId = exchange_reference_id
    notice.noticeSourceUniqueId = exchange_reference_id
    notice.eventInitialUrl = f'https://www.matbarofex.com.ar{notice_table_row.find("div", class_="card-body").a["href"]}'
    notice.eventSubject = english_subject
    notice.eventPublishDate = convert_date(notice_table_row.find('time', class_='datetime').text, exchange)
    notice.stormKey = f'{notice.exchangeSourceName}|{notice.noticeSourceUniqueId}'
    notice.valid_notice = True if notice.noticeSourceUniqueId else False

    return notice
