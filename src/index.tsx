import { NativeModules } from 'react-native';

type RnSmsRetrieverType = {
  multiply(a: number, b: number): Promise<number>;
  requestPhoneNumber(): Promise<boolean>;
  startSmsRetriever(): Promise<boolean>;
};

const { RnSmsRetriever } = NativeModules;

export default RnSmsRetriever as RnSmsRetrieverType;
