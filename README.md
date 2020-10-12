# rn-sms-retriever

React Native implementation of Android SMS Retriever

## Installation

```sh
npm install rn-sms-retriever
```

## Usage

```js
import RnSmsRetriever from "rn-sms-retriever";

export default function App() {

  const SMS_EVENT = "me.furtado.smsretriever:SmsEvent"

  React.useEffect(() => {
    let smsListener = undefined
    async function innerAsync() {
      const phoneNumberRes = await RnSmsRetriever.requestPhoneNumber();
      smsListener = DeviceEventEmitter.addListener(SMS_EVENT, (data: any) => {
        console.log(data, "SMS value")
      })
      const smsListener = await RnSmsRetriever.startSmsRetriever();
    }
    innerAsync()
    return () => {
      if (smsListener)
        smsListener.remove()
    }
  }, []);

  //....
}
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
