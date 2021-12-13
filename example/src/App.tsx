import * as React from 'react';
import {
  StyleSheet,
  View,
  DeviceEventEmitter,
  EmitterSubscription,
  Platform,
  Text,
} from 'react-native';
import RnSmsRetriever, { SMS_EVENT } from 'rn-sms-retriever';

export default function App() {
  React.useEffect(() => {
    let smsListener: undefined | EmitterSubscription;
    async function innerAsync() {
      // get list of available phone numbers
      const selectedPhone = await RnSmsRetriever.requestPhoneNumber();
      console.log('Selected Phone is : ' + selectedPhone);
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
    // only to be used with Android
    if (Platform.OS === 'android') innerAsync();
    return () => {
      // remove the listsner on unmount
      smsListener?.remove();
    };
  }, []);

  return <View style={styles.container} >
    <Text>Listener Test</Text>
  </View>;
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
