import 'package:flutter/material.dart';
import 'package:frontend/screens/memorial/memorial_letter_upload.dart';
import 'package:frontend/view_models/dio_api_request_examples.dart';
import 'package:frontend/view_models/memorial_view_models/memorial_detail_letterTab_viewmodel.dart';
import 'package:provider/provider.dart';

class LetterTab extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
        create: (context) => LetterTabViewModel(),
        child: Consumer<LetterTabViewModel>(
          builder: (context, viewModel, child) {
            return NotificationListener<ScrollNotification>(
                onNotification: (scroll) {
                  if (scroll.metrics.pixels >=
                      scroll.metrics.maxScrollExtent - 50) {
                    viewModel.loadMore();
                    // debugPrint('더 불러오기');
                    // debugPrint(viewModel.letters.length.toString());
                  } else {
                    // debugPrint('스크롤 위치 ${scroll.metrics.pixels}');
                  }
                  return false;
                },
                child: CustomScrollView(
                  slivers: <Widget>[
                    SliverGrid(
                      gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                        crossAxisCount: 2,
                        mainAxisSpacing: 10,
                        crossAxisSpacing: 10,
                        childAspectRatio: 2,
                      ),
                      delegate: SliverChildBuilderDelegate(
                        (BuildContext context, int index) {
                          if (index == 0) {
                            return Card(
                                child: InkWell(
                              onTap: () => {
                                Navigator.push(
                                  context,
                                  MaterialPageRoute(
                                    builder: (context) =>
                                        MemorialLetterUpload(),
                                  ),
                                )
                              },
                              child: Column(
                                  children: [Icon(Icons.add), Text('편지 추가')]),
                            ));
                          } else {
                            return Letter(
                              key: ValueKey(viewModel.letters[index - 1].memorialLetterSeq),
                              content:
                                  (viewModel.letters[index - 1].content != null)
                                      ? viewModel.letters[index - 1].content
                                      : '',
                              date: viewModel.letters[index - 1].writtenDate,
                              nickname: viewModel.letters[index - 1].nickname,
                              // memorialLetterSeq: viewModel.letters[index - 1].memorialLetterSeq.toString(),
                            );
                          }
                        },
                        childCount: viewModel.letters.length + 1,
                      ),
                    )
                  ],
                ));
          },
        ));
  }
}

class Letter extends StatelessWidget {
  final String? nickname;
  final String? content;
  final String date;
  // final int memorialLetterSeq;

  Letter({
    Key? key,
    this.nickname,
    this.content,
    required this.date,
    // required this.memorialLetterSeq,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    OverlayEntry? overlayEntry;
    return Card(
      child: GestureDetector(
        onLongPress: () {
          overlayEntry = OverlayEntry(
            builder: (context) => Container(
              height: MediaQuery.of(context).size.height * 0.1,
              width: MediaQuery.of(context).size.width * 0.1,
              child: Material(
                color: Colors.transparent,
                child: Container(
                  decoration: BoxDecoration(
                    image: DecorationImage(
                      image: AssetImage('assets/images/letter.png'),
                      fit: BoxFit.scaleDown,
                    ),
                  ),
                ),
              ),
            ),
          );
          Overlay.of(context).insert(overlayEntry!);
        },
        onLongPressEnd: (details) {
          overlayEntry?.remove();
        },
        child: Column(
          children: [
            Text(nickname ?? ''),
            Text(content ?? ''),
            Text(date),
          ],
        ),
      ),
    );
  }
}
