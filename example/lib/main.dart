import 'package:flutter/material.dart';
import 'package:flutter_bugly/flutter_bugly.dart';

void main() => FlutterBugly.postCatchedException(() => runApp(MyApp()));

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(home: HomePage());
  }
}

class HomePage extends StatefulWidget {
  const HomePage({Key? key}) : super(key: key);

  @override
  State<StatefulWidget> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    FlutterBugly.init(
      androidAppId: "your app id",
      iOSAppId: "your app id",
    ).then((_result) {
      setState(() {
        _platformVersion = _result.message;
        print(_result.appId);
      });
    });
    // // 当配置 customUpgrade=true 时候，这里可以接收自定义升级
    // FlutterBugly.onCheckUpgrade.listen((_upgradeInfo) {
    //   _showUpdateDialog(
    //     _upgradeInfo.newFeature,
    //     _upgradeInfo.apkUrl!,
    //     _upgradeInfo.upgradeType == 2,
    //   );
    // });
    FlutterBugly.setUserId("user id");
    FlutterBugly.putUserData(key: "key", value: "value");
    int tag = 9527;
    FlutterBugly.setUserTag(tag);
    // autoCheckUpgrade 为 true 时，可以不用调用
    // if (mounted) _checkUpgrade();
  }

  @override
  void dispose() {
    FlutterBugly.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Plugin example app')),
      body: GestureDetector(
        onTap: () {},
        child: Center(
          child: Text('init result: $_platformVersion\n'),
        ),
      ),
    );
  }
}
