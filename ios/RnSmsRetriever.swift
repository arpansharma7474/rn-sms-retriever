@objc(RnSmsRetriever)
class RnSmsRetriever: NSObject {

    @objc(multiply:withB:withResolver:withRejecter:)
    func multiply(a: Float, b: Float, resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        resolve(a*b)
    }

    @objc override static func requiresMainQueueSetup() -> Bool {
        return false
    }
}
