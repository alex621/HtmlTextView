# HtmlTextView for Android

HtmlTextView is a library built on top of TextView in Android for displaying HTML with TextView. You can use [Html.fromHtml()](https://developer.android.com/reference/android/text/Html.html#fromHtml(java.lang.String)) to render HTML by default. However the only way to show image is implementing Html.ImageGetter and returning a drawable from it. What are the drawbacks?

1. It means more workload before you can bind a click event to it.
2. No recycling logic! That means you could possibly face OOM problem.
3. You miss the fancy functions from image libraries like Fresco, Glide...

HtmlTextView is here to solve these problems.

### Why not WebView?

Seriously? You wanna use WebView in RecyclerView?


## Usage

To add a dependency using Gradle:

```
  compile 'com.westkit.htmltextview:htmltextview:0.1.1'
  
  //Optional
  compile 'com.westkit.htmltextview:htmltextview-default-adapter:0.1.1'
```

What is the default adapter? Using HtmlTextView, you need to plug an adapter into the view in order to display resources like `<img>` `<video>` . Default adapter is an out-of-the-box solution for it. It uses Fresco to display images. Unless you want to use other libraries, you may just stick with the default adapter.

## Getting started

Use it in XML like other views

```xml
<com.westkit.htmltextview.HtmlTextView
            android:id="@+id/tv"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />
```

Set the data in code.

```java
final HtmlTextView tv = (HtmlTextView) findViewById(R.id.tv);
tv.setAdapter(new HtmlTextViewDefaultAdapter());
tv.setHtml(html);
```

That's it. Assuming that you have defined width and height for your `<img>`.

Yes! You need to give me the width and height in order to show the images, either inside the HTML or providing another data object, like the following:

```java
MapDataSupplier data = new MapDataSupplier(); data.put("https://placehold.it/350x150", new ImgData(350, 150));

tv.setDataSupplier(data);
```

## How to recycle

Extend the adapter, either the default one or the HtmlTextViewAdapter class. There is a `recycleImg()` method ready for you. It will be called when the `<img>` is completely out of screen. By default, the View for displaying `<img>` will be detached from the view tree once it is out of screen. If your library recycles the image when the View is detached, you should not be needed to code extra lines to recycle it.

However, you **must** call `tv.recycleCheck();` when there is any movement, like scrolling, for the HtmlTextView.

For instance, when you place the HtmlTextView inside a ScrollView, you need to call `recycleCheck()` on scroll event. You may take a look at the application inside the `app/` folder.

```java
ObservableScrollView scroller = (ObservableScrollView) findViewById(R.id.scroller);
scroller.setScrollViewListener(new ObservableScrollView.ScrollViewListener() {
  @Override
  public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
      tv.recycleCheck();
  }
});
```

ObservableScrollView is provided in this library as well. Use it if you want ;)

## License

WTFPL. Do what the fuck you want to.