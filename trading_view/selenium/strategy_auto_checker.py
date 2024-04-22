# TradeView Strategy Auto Checker
# Usage:
# $ pytest ./strategy_auto_checker.py
#
# 参考
# profile指定時のクラッシュ解決
#  - https://groups.google.com/g/seleniumjp/c/HETX_iiYQG4?pli=1
# Selenium API(逆引き)
#  - https://www.seleniumqref.com/api/webdriver_gyaku.html#google_vignette
#


import pytest
import time
import json
import codecs
import datetime
import strategy_parameter
import numpy as np

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.support import expected_conditions
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.desired_capabilities import DesiredCapabilities
from selenium.webdriver.chrome.options import Options
now = datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=9), 'JST')).strftime('%Y%m%d-%H%M%S')

class TestStrategytest():

  def setup_method(self, method):
    options = Options()
    if strategy_parameter.IS_HEADLESS :
      options.add_argument('--headless')
    options.add_argument('window-size=1200,1600')
    options.add_argument('--user-data-dir=C:/Users/shuni/AppData/Local/Google/Chrome/UserDataSelenium')
    options.add_argument('--profile-directory=Profile1')
    options.binary_location = "C:/Program Files/Google/Chrome/Application/chrome.exe"
    self.driver = webdriver.Chrome(options=options)
    self.driver.implicitly_wait(5)
    self.driver.add_cookie({"name": "sessionid", "value": strategy_parameter.BROWSER_SESSIONID, "domain": ".tradingview.com"})

  def teardown_method(self, method):
    self.driver.quit()
  
  def test_strategytest(self):
    # パラメータ生成
    if strategy_parameter.PARAMETER_1_USE :
      range1 = np.arange(strategy_parameter.PARAMETER_1_START_VALUE, strategy_parameter.PARAMETER_1_MAX_VALUE, strategy_parameter.PARAMETER_1_INCREMENTAL_VALUE)
    else :
      range1 = np.arange(0, 1, 1)

    if strategy_parameter.PARAMETER_2_USE :
      range2 = np.arange(strategy_parameter.PARAMETER_2_START_VALUE, strategy_parameter.PARAMETER_2_MAX_VALUE, strategy_parameter.PARAMETER_2_INCREMENTAL_VALUE)
    else :
      range2 = np.arange(0, 1, 1)

    # ブラウザ表示
    self.driver.get("https://jp.tradingview.com/chart/WmWsNqt4/")
    time.sleep(strategy_parameter.START_WAITING_SEC)

    # プロパティ画面表示
    self.driver.find_element(By.CSS_SELECTOR, ".apply-common-tooltip:nth-child(1) > .icon-bYDQcOkp > svg").click()

    # ファイルに追記
    file_prefix = self.driver.find_element(By.XPATH, '//*[@id="overlap-manager-root"]/div/div/div[1]/div/div[1]/div/div').text

    print("{0}\t{1}\t{2}\t{3}\t{4}\t{5}\t{6}\t{7}\t{8}\t{9}".format(
      "パラメータ1", "パラメータ2", "純利益", "終了したトレードの合計", "勝率", "プロフィットファクター", "最大ドローダウン", "平均トレード", "平均バー数", "純利益割合"
    ), file=codecs.open("./log/strategy-{0}-{1}.tsv".format(file_prefix, now), 'a', 'utf-8'))

    # パラメータ変更
    for i in range2 :

      if strategy_parameter.PARAMETER_2_USE :
        # パラメータ2変更
        element = self.driver.find_element(By.XPATH, strategy_parameter.PARAMETER_2_PATH)
        element.send_keys( Keys.CONTROL + "a" )
        element.send_keys( Keys.DELETE )
        element.send_keys(str(i))

      for j in range1 :
        if strategy_parameter.PARAMETER_1_USE :
          # パラメータ1変更
          element = self.driver.find_element(By.XPATH, strategy_parameter.PARAMETER_1_PATH)
          element.send_keys( Keys.CONTROL + "a" )
          element.send_keys( Keys.DELETE )
          element.send_keys(str(j))

          # パラメータ変更確定
          self.driver.find_element(By.XPATH, '//*[@id="overlap-manager-root"]/div/div/div[1]/div/div[4]').click()
          time.sleep(3)

        # パフォーマンス結果取得
        if self.driver.find_element(By.XPATH, '//*[@id="bottom-area"]/div[4]/div/div[2]/div[1]/div[2]').text == 'データなし' :
          print("{0}\t{1}".format(
            j, i
          ), file=codecs.open("./log/strategy-{0}-{1}.tsv".format(file_prefix, now), 'a', 'utf-8'))

        else :
          report = self.driver.find_element(By.XPATH, '//*[@id="bottom-area"]/div[4]/div/div[2]/div/div[1]')
          junrieki = report.find_element(By.XPATH, 'div[1]/div[2]/div[1]').text
          junriekiP = report.find_element(By.XPATH, 'div[1]/div[2]/div[2]').text
          tradeSum = report.find_element(By.XPATH, '  div[2]/div[2]/div[1]').text
          shoritsuP = report.find_element(By.XPATH, 'div[3]/div[2]/div[1]').text
          profit = report.find_element(By.XPATH, 'div[4]/div[2]/div[1]').text
          drawDown = report.find_element(By.XPATH, 'div[5]/div[2]/div[1]').text
          drawDownP = report.find_element(By.XPATH, 'div[5]/div[2]/div[2]').text
          avgtrade = report.find_element(By.XPATH, 'div[6]/div[2]/div[1]').text
          avgtradeP = report.find_element(By.XPATH, 'div[6]/div[2]/div[2]').text
          avtTradeBar = report.find_element(By.XPATH, 'div[7]/div[2]/div[1]').text
          junriekiDrwawP = '=SUBSTITUTE(SUBSTITUTE(offset($A$1,row()-1,2),"%","")," ","") / SUBSTITUTE(offset($A$1,row()-1,6),"%","")'

          print("{0}\t{1}\t{2}\t{3}\t{4}\t{5}\t{6}\t{7}\t{8}\t{9}".format(
            i, j, junriekiP, tradeSum, shoritsuP, profit, drawDownP, avgtradeP, avtTradeBar, junriekiDrwawP
          ), file=codecs.open("./log/strategy-{0}-{1}.tsv".format(file_prefix, now), 'a', 'utf-8'))
