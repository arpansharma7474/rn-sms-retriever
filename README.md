# rn-sms-retriever

React Native implementation of Android SMS Retriever. No READ_SMS permisssion is required now, instead we have to use [SMS retriever](https://developers.google.com/identity/sms-retriever/overview).

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
    let smsListener: undefined | EmitterSubscription;
    async function innerAsync() {
      // get list of available phone numbers
      await RnSmsRetriever.requestPhoneNumber();
      // set Up SMS Listener;
      smsListener = DeviceEventEmitter.addListener(SMS_EVENT, (data: any) => {
        console.log(data, 'SMS value');
      });
      // start Retriever;
      await RnSmsRetriever.startSmsRetriever();
    }
    innerAsync();
    return () => {
      // remove the listsner on unmount
      smsListener?.remove();
    };
  }, []);
  //....
}
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
