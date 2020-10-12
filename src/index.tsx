import { NativeModules } from 'react-native';

type RnSmsRetrieverType = {
  multiply(a: number, b: number): Promise<number>;
};

const { RnSmsRetriever } = NativeModules;

export default RnSmsRetriever as RnSmsRetrieverType;
