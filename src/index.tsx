import { NativeModules } from 'react-native';

type RnSmsRetrieverType = {
  multiply(a: number, b: number): Promise<number>;
  requestPhoneNumber(): Promise<string>;
  startSmsRetriever(): Promise<boolean>;
  getAppHash(): Promise<string>;
};

const { RnSmsRetriever } = NativeModules;

export const SMS_EVENT = 'me.furtado.smsretriever:SmsEvent';

export default RnSmsRetriever as RnSmsRetrieverType;
