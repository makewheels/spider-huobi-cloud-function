```shell
mvn compile assembly:single
```

### example config:

```json
{
  "version": "1",
  "missionName": "huobi",
  "target": {
    "type": "object-storage",
    "endpoint": "oss-cn-beijing.aliyuncs.com",
    "region": "cn-beijing",
    "bucketName": "web-spider",
    "path": "spider/${missionName}/${requestName}/${GMTString}.json"
  },
  "requests": [
    {
      "requestName": "sell-usdt-cny",
      "source": {
        "method": "GET",
        "url": "https://otc-api-hk.eiijo.cn/v1/data/trade-market?coinId=2&currency=1&tradeType=sell&currPage=1&payMethod=0&acceptOrder=-1&country=&blockType=general&online=1&range=0&amount="
      }
    },
    {
      "requestName": "buy-usdt-cny",
      "source": {
        "method": "GET",
        "url": "https://otc-api-hk.eiijo.cn/v1/data/trade-market?coinId=2&currency=1&tradeType=buy&currPage=1&payMethod=0&acceptOrder=-1&country=&blockType=general&online=1&range=0&amount="
      }
    }
  ]
}
```