package io.github.kichikuou.xsystem4 // あなたのパッケージ名に合わせてください

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import org.libsdl.app.SDLActivity

class XSystem4WithPadActivity : SDLActivity() { // SDLActivityを継承
    //    XSystem4Activity
    companion object {
        const val EXTRA_GAME_ROOT = "GAME_ROOT"
        const val EXTRA_SAVE_DIR = "SAVE_DIR"
    }

    override fun getLibraries(): Array<String> {
        return arrayOf("SDL2", "xsystem4")
    }

    override fun getArguments(): Array<String> {
        val saveFolder = intent.getStringExtra(EXTRA_SAVE_DIR)!!
        val gameRoot = intent.getStringExtra(EXTRA_GAME_ROOT)!!
        return arrayOf("--save-folder", saveFolder, "--save-format=rsm", gameRoot)
    }
    // /XSystem4Activity

    private var overlayControlsView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // SDLActivity.onCreate() が mLayout (FrameLayout) を初期化するので、
        // スーパークラスの onCreate を先に呼び出す必要があります。
        super.onCreate(savedInstanceState)

        // 画面をランドスケープに固定 (任意)
        // requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        // または ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        // オーバーレイUIのセットアップ
        setupOverlayControls()
    }

    private fun setupOverlayControls() {
        // SDLActivity のメインレイアウトを取得
        // SDLActivity.mLayout は protected なので、直接アクセス可能
        // mLayout は FrameLayout であると想定
        val mainLayout = mLayout
        if (mainLayout == null) {
            Log.e("XSystem4Activity", "SDL Main layout (mLayout) is null. Cannot add overlay.")
            return
        }

        val inflater = LayoutInflater.from(this)
        overlayControlsView = inflater.inflate(R.layout.overlay_controls, mainLayout, false)

        // UI要素への参照取得とリスナー設定
        overlayControlsView?.let { ovView ->
            // --- 左側のコントロール ---
            val joystickArea = ovView.findViewById<FrameLayout>(R.id.joystick_area)
            // ジョイスティックエリアのタッチイベント処理 (例)
            joystickArea.setOnTouchListener { v, event ->
                // ジョイスティックのロジックをここに実装
                // event.x, event.y をネイティブコードに送るなど
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> Log.d(
                        "XSystem4Activity",
                        "nativeSendJoystickEvent: x=${event.x}, y=${event.y}, pressed=true"
                    )

                    MotionEvent.ACTION_MOVE -> Log.d(
                        "XSystem4Activity",
                        "nativeSendJoystickEvent: x=${event.x}, y=${event.y}, pressed=true"
                    )

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> Log.d(
                        "XSystem4Activity",
                        "nativeSendJoystickEvent: x=0f, y=0f, pressed=false"
                    ) // リセット
                }
                true // イベントを消費
            }

            // 十字キーボタン
            ovView.findViewById<Button>(R.id.dpad_up).setOnClickListener {
                Log.d("XSystem4Activity", "nativeSendDpadEvent: up_pressed")
            }
            // 他の十字キーボタンも同様に設定 (DOWN, MOVE, UP イベントを考慮する場合は OnTouchListener を使う)
            // 簡単のためここでは onClickListener を使用
            ovView.findViewById<Button>(R.id.dpad_down).setOnClickListener {
                Log.d("XSystem4Activity", "nativeSendDpadEvent: down_pressed")
            }
            ovView.findViewById<Button>(R.id.dpad_left).setOnClickListener {
                Log.d("XSystem4Activity", "nativeSendDpadEvent: left_pressed")
            }
            ovView.findViewById<Button>(R.id.dpad_right).setOnClickListener {
                Log.d("XSystem4Activity", "nativeSendDpadEvent: right_pressed")
            }


            // --- 右側のコントロール ---
            ovView.findViewById<Button>(R.id.button_action_a).setOnClickListener {
                Log.d("XSystem4Activity", "nativeSendButtonEvent: A_pressed")
            }
            ovView.findViewById<Button>(R.id.button_action_b).setOnClickListener {
                Log.d("XSystem4Activity", "nativeSendButtonEvent: B_pressed")
            }
            ovView.findViewById<Button>(R.id.button_menu).setOnClickListener {
                Log.d("XSystem4Activity", "nativeSendButtonEvent: Menu_pressed")
                // 例: メニューボタンが押されたらオーバーレイを非表示にする
                // toggleOverlayVisibility()
            }

            // メインレイアウトにオーバーレイビューを追加
            // SDLSurface (mSurface) の上に重なるようにする
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            mainLayout.addView(ovView, params)
            Log.d("XSystem4Activity", "Overlay controls added.")

            // 初期状態は表示 (必要に応じて変更)
            setOverlayVisibility(true)
        }
    }

    // オーバーレイの表示・非表示を切り替えるメソッド（例）
    fun setOverlayVisibility(visible: Boolean) {
        overlayControlsView?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun toggleOverlayVisibility() {
        overlayControlsView?.let {
            it.visibility = if (it.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    // 画面の向きが変わったときの処理 (オプション)
    // ランドスケープ固定なら不要な場合もあるが、UI調整が必要な場合
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // ここでランドスケープ/ポートレートに応じてUIの表示/非表示やレイアウト調整を行う
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d("XSystem4Activity", "Now in Landscape")
            // 必要ならランドスケープ用の調整
            setOverlayVisibility(true) // ランドスケープでは表示するなど
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d("XSystem4Activity", "Now in Portrait")
            // ポートレートでは非表示にするなど
            // setOverlayVisibility(false)
        }
    }
}
