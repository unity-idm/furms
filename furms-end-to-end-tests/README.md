To run this module tests via jenkins two thing have to be done:
1. System env named "chrome-driver-path" have to be set to system path to chrome driver.
2. Variable "furmsClientSecret" have to be assigned through the command line. For example:
-DfurmsClientSecret="TOP_SECRET"

Additionally, it is possible to add chrome driver options via commend line. For example:
-Dselenium.opts="no-sandbox,allow-insecure-localhost"