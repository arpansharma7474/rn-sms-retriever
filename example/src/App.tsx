import * as React from 'react';
import {
  StyleSheet,
  View,
  DeviceEventEmitter,
  EmitterSubscription,
} from 'react-native';
import RnSmsRetriever from 'rn-sms-retriever';

export default function App() {
  const SMS_EVENT = 'me.furtado.smsretriever:SmsEvent';

  React.useEffect(() => {
    let smsListener: undefined | EmitterSubscription;
    async function innerAsync() {
      // get list of available phone numbers
      await RnSmsRetriever.requestPhoneNumber();
      // get App Hash
      const hash = await RnSmsRetriever.getAppHash();
      console.log('Your App Hash is : ' + hash);
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

  return <View style={styles.container} />;
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
