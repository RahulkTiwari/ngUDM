# README

## Introduction

This application scrapes exchange notifications for a list of supported exchanges. For the relevant notifications a service desk ticket is created
which is assigned to the exchange owner. Using this ticket the notification can be followed up on. For example, the events resulting from the 
notification can be created manually and imported into the database (enData collection) through the ENS import template. 

Intended usage is that this application runs periodically (e.g. daily) for each of the exchange in one or a batch of exchanges. A scheduler job like
Airflow can be used to start this application.

The notification retrieved by this application are also stored in the workItem collection in the configured database.

Note that one of the fields which is retrieved is the publication date. If the publication date cannot be extracted from the notification itself then 
the processing time is used as publication date.

All the exchange sources not covered by the automated processing are supported. See [supported exchanges](#appendix--supported-exchanges) for a 
complete list.

## Installation and requirements

The application is written in Python and requires version 3.8[^1] or higher. The following side packages (also listed in the ```requirements.txt```) 
need to be installed:

- pandas
- pymongo
- requests
- bs4
- beautifulsoup4
- playwright
- request_html
- pyee
- googletrans[^2]
- office365

[^1]: The application has been tested versus Python 3.8 and 3.9.

[^2]: Note that googletrans version ```4.0.0rc1``` or higher is required. This release contains certain bug fixes used by the application.

Side packages can be installed by running:
```python -m pip install -r requirements.txt```

After all required packages are installed, playwright needs an additional installation step by running:
```playwright install```

The application can be installed by extracting the package in a folder of choise.

## Configuration

The application uses one configuration file, i.e. ```configurations.ini``` located in the root folder of the project. After installation the following
properties need to be reviewed and updated to usage and the installation environment:

### LOG section:

- log_dir: folder where to write the log file to. Make sure the application has the proper write to this folder!
- level: level of logging. In production the recommended setting is INFO.
- root_filename: the log file consists of a rootfile name sufixed with a datetime. The root filename can be configured using this property. 
The filename will look like: ```ens_notices2022-12-21161847.log```

### SHAREPOINT section:

- credential_file_path: provide the path, including filename, where the credentials to Sharepoint are stored. The file should contain two properties:
  - username
  - password

For example,
```
username=my.name@smartstreamrdu.com
password=mySuperSecretPassword
```

A template file ```credentials_sample.txt``` is included in the ```.\resources``` folder of the project.

### CONNECTION section:

Provide the connection details of the server where to store the notifications. This collection is used to determine if a notification has already
been processed.

### PROCESSING section:

- lookback_period: The positive integer reflecting the number of days to look back for notifications. See [Notification filtering](#notification-filtering)
- retries_number: Positive integer indicating how many times to retry scraping a website for an exchange when intially scraping resulted in an error.
The default number is 3 retries.
- website_timeout: Time in milliseconds to wait for a website to respond to a request. Default is 30000 milliseconds (i.e. 30 seconds).

<em><mark style="background-color:rgb(153, 255, 51)">Do not change other properties, since they may affect runtime of the application!</mark></em>

## Notification filtering

The application allows two ways of filtering notifications:

- by lookback period
- by regular expression on the subject

### Lookback period

As mentioned in [configuration section](#processing-section-) the lookback integer number reflects the publication date days prior to today to the 
even publish date. For example, if event publish date is December 21 and the lookback is 4 days then every notice from and including December 17 is 
processed. Note that notices are not processed multiple times when the same notice is evaluated and is in scope in several runs in the lookback 
period. Already processed and stored notices are marked as such.

Note that if the event publish date is not available from the notification then today's date is used. This in the assumption that the application runs
at least once a day.

### Regular expression

The ```regex.xlsx``` Excel file contains a sheet for all of the supported exchanges. The ```Positive regex``` column should contain a regular 
expression which will be applied on the subject of the notification. If the subject matches that regular expression then the notification is 
processed, i.e. a service desk ticket is created and an entry is stored in the workItem collection. In contrast, all other notification will 
be skipped from processing.

When a change is made in the regex to cover missed notices, these notices will be picked up if the change occurs in the lookback period. If earlier
notices need to be evaluated then the lookback period needs to be (temporarily) adjusted.

In case the website posts the notification in a non-English language (as seen for some Chinese, Korean and Mexican exchanges) then the subject of the 
notification is translated to English before the regex is applied. The English translated subject will also be used in the ticket.
If for whatever reason automated translation of the subject fails, then the subject will have the default value 'Unable to translate subject'. These
notifications will be processed and a ticket created to be evaluated manually.

The ```regex.xlsx``` is stored on Sharepoing on the following location: 
[link to regex Excel](https://smartstreamstpcom.sharepoint.com/:x:/s/ReferenceDataUtility/EeGZnfWJhMJPna2aoNwVFjkBHOPHtkH4BhteYX2VI0m2Og?e=UekcNo).

## Usage

The application takes one mandatory argument, i.e. ```-e```, ```--exchanges```. The value for this argument is one exchange or a list of
comma separated exchanges from the in the above table listed exchanges. For example: ```--exchanges=asx,borsa_istanbul_news,b3_circular,athexgroup```
Since the application can be executed by running the ```main``` module, the run command then would be:

```python -m main --exchanges=asx,borsa_istanbul_news,b3_circular,athexgroup```

## Known issues

At this moment there are no issues known. If you encounter issues or have questions, please contact:

- [Marc de Vent](mailto:marc.devent@smartstreamrdu.com) or
- [Jaime Rengifo](mailto:jaime.rengifo@smartstreamrdu.com)

## Appendix: supported exchanges


|exchange|exchangeSourceName|exchange|exchangeSourceName|exchange|exchangeSourceName|
|--------|------------------|--------|------------------|--------|------------------|
|gpw_news|GPW_MAIN_MARKET_NEWS|jpx_securities_options|JPX_SECURITY_OPTIONS_COAX|nasdaq_copenhagen|NASDAQ_OMX_NORDIC_MARKET_NOTICES_COPENHAGEN|
|gpw_communiques_resolutions|GPW_COMMUNIQUES_RESOLUTIONS|jpx_public_comments|JPX_RULES_TRADING_PARTICIPANTS_PUBLIC_COMMENTS|nasdaq_stockholm|NASDAQ_OMX_NORDIC_MARKET_NOTICES_STOCKHOLM|
|tfx_newsfile_notice|TFX_NEWSFILE_NOTICE_FROM_TFX|jpx_market_news|JPX_MARKET_NEWS|nasdaq_helsinki|NASDAQ_OMX_NORDIC_MARKET_NOTICES_HELSINKI|
|tase_notices|TASE_NOTICES|jpx_info_news|JPX_INFORMATION_NEWS|nasdaq_it|NASDAQ_OMX_NORDIC_IT_NOTICES|
|taifex_press_releases|TAIFEX_PRESS_RELEASES|dgcx_notices|DGCX_NOTICES|nasdaq_omx|NASDAQ_OMX_NORDIC_NEWS_NOTICES|
|shfe_news_circulars|SHFE_NEWS_CIRCULARS|mgex_news_rel|MGEX_NEWS_RELEASES_MARKET_ALERTS|tfex|TFEX_TRADING_NOTICE|
|matbarofex_documents|MATBAROFEX_DOCUMENTS|mgex_announcements|MGEX_ANNOUNCEMENTS|meff_commodity_notices|MEFF_COMMODITY_DERIVATIVES_MARKET_NOTICES|
|omip_publications|OMIP_PUBLICATIONS|cffex_notices|CFFEX_NOTICES|meff_financial_notices|MEFF_FINANCIAL_DERIVATIVES_MARKET_NOTICES|
|cboe_us_futures_updates|CBOE_US_FUTURES_NOTICES_PRODUCT_UPDATES|cboe_euro_derivates|CBOE_EUROPE_DERIVATIVES_NOTICES_RELEASE_NOTES|meff_rules_regulations|MEFF_RULES_REGULATIONS_CIRCULARS|
|nzx_announcements|NZX_NZSX_ANNOUNCEMENTS|budapest_news|BUDAPEST_NEWS_RELEASES|mexder_avisos|MEXDER_AVISOS|
|nasdaq_dubai_notices|NASDAQ_DUBAI_REG_FRAMEWORK_NOTICES|borsa_italia_idem|BORSA_ITALIANA_IDEM_NOTICES|tmx_advisory_notices|TMX_ADVISORY_NOTICES|
|nasdaq_dubai_circulars|NASDAQ_DUBAI_REG_FRAMEWORK_CIRCULARS|borsa_italiana_market_connect|BORSA_ITALIANA_MARKET_CONNECT_NOTICES|tmx_technical_notices|TMX_TECHNICAL_NOTICES|
|mcxinda_trading_surveillance|MCXINDIA_CIRCULARS_TRADING_SURVEILLANCE|asx|ASX_NOTICES|czce_derivatives_announcements|CZCE_DERIVATIVES_NEWS_ANNOUNCEMENTS|
|lme_press_release|LME_PRESS_RELEASE|kap_pdp|BORSA_ISTANBUL_PDP|czce_exchange_announcements|CZCE_EXCHANGE_NEWS_ANNOUNCEMENTS_NOTICES|
|lme_member_notice|LME_MEMBER_NOTICE|borsa_istanbul_news|BORSA_ISTANBUL_ALL_NEWS|dce_clearing_notices|DCE_TRADING_CLEARING_EXCHANGE_NOTICES|
|lme_clearing_circ|LME_CLEARING_CIRCULAR|b3_circular|B3_CIRCULAR_LETTERS_EXTERNAL_COMMS|dce_exchange_news|DCE_TRADING_NEWS_CENTER_EXCHANGE_NEWS|
|krx_koscom|KRX_KOSCOM_NOTICE_MARKET_NOTICE|athexgroup|ATHEXGROUP_DERIV_CORP_ACTIONS|dce_business_announcements|DCE_BUSINESS_ANNOUNCEMENTS_NOTICES|
|krx_kind|KRX_KIND|bse|BSE_MARKETINFO_NOTICES_CIRCULARS|dce_see_dashang|DCE_TRADING_NEWS_CENTER_MEDIA_SEE_DASHANG|
|jse_market_notices|JSE_MARKET_NOTICES|nse|NSE_EXCHANGE_COMMUNICATION_CIRCULARS|euronext_notices|EURONEXT_NOTICES|
|euronext_info_flashes|EURONEXT_INFO_FLASHES|taifex_notices|TAIFEX_NOTICES|taifex_ssf_oca|TAIFEX_SSF_OPTION_CONTRACT_ADJUSTMENT|
