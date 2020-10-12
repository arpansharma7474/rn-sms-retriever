import * as React from 'react';
import { StyleSheet, View, Text, DeviceEventEmitter } from 'react-native';
import RnSmsRetriever from 'rn-sms-retriever';

export default function App() {

  const SMS_EVENT = "me.furtado.smsretriever:SmsEvent"

  React.useEffect(() => {
    let res = undefined
    async function innerAsync() {
      // const res = await RnSmsRetriever.requestPhoneNumber();
      res = DeviceEventEmitter.addListener(SMS_EVENT, (data: any) => {
        console.log(data, "SMS value")
      })
      const res2 = await RnSmsRetriever.startSmsRetriever();
      console.log(res2, "res")
    }
    innerAsync()
    return () => {
      if (res)
        res.remove()
    }
  }, []);

  return (
    <View style={styles.container}>

    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
