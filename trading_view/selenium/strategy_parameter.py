# ブラウザ Cookie（sessionid）
BROWSER_SESSIONID = ''

# ヘッドレスで実行するか
IS_HEADLESS = False

# 起動後変更待ち時間
START_WAITING_SEC = 10

# パラメータ設定
PARAMETER_1_USE = True
PARAMETER_2_USE = True

# 所要時間 繰り返し回数 * 繰り返し回数 * 3sec
# およそ1150件 / hour
PARAMETER_1_PATH = '//*[@id="overlap-manager-root"]/div/div/div[1]/div/div[3]/div/div[2]/div/span/span[1]/input'
PARAMETER_1_START_VALUE = 1
PARAMETER_1_MAX_VALUE = 100
PARAMETER_1_INCREMENTAL_VALUE = 2

PARAMETER_2_PATH ='//*[@id="overlap-manager-root"]/div/div/div[1]/div/div[3]/div/div[4]/div/span/span[1]/input'
PARAMETER_2_START_VALUE = 0.1
PARAMETER_2_MAX_VALUE = 7
PARAMETER_2_INCREMENTAL_VALUE = 0.05