package edu.wustl.Arc.app.arc.model

import android.graphics.Bitmap
import android.util.Log
import edu.wustl.arc.core.Application
import edu.wustl.arc.core.LoadingDialog
import edu.wustl.arc.navigation.NavigationManager
import edu.wustl.arc.study.StateMachineAlpha
import edu.wustl.arc.study.Study
import edu.wustl.arc.study.TestSession
import java.io.File
import java.io.FileOutputStream

public interface SessionCompleteListener {
    fun onSessionComplete(signatureList: ArrayList<File>, session: TestSession)
}

open class ArcMtbStateMachine : StateMachineAlpha() {

    companion object {
        const val jpegQuality = 80
    }

    var listener: SessionCompleteListener? = null
    private val signatureList: ArrayList<File> = arrayListOf()

    override fun initialize() {
        super.initialize()
        state.lifecycle = LIFECYCLE_INIT
        state.currentPath = PATH_TEST_NONE
    }

    override fun submitSignature(signature: Bitmap?) {
        val sigUnwrapped = signature ?: run { return }

        val signatureDir = Application.getInstance().appContext.filesDir.resolve("signatures")
        if (!signatureDir.exists()) {
            signatureDir.mkdir()
        }
        val signatureFile = signatureDir.resolve("signature${signatureList.size}.jpeg")

        val fos = FileOutputStream(signatureFile);
        val success = sigUnwrapped.compress(Bitmap.CompressFormat.JPEG, jpegQuality, fos)
        if (!success) {
            Log.e(tag, "Error writing signature image to file")
        }
        fos.flush()
        fos.close()
        signatureList.add(signatureFile)
    }

    override fun submitTest(testSession: TestSession?) {
        testSession?.let {
            listener?.onSessionComplete(signatureList, it)
        }
    }

    override fun endOfPath() {
        Log.i(tag, "gather data from test")
        // set up a loading dialog in case this takes a bit
        val dialog = LoadingDialog()
        dialog.show(NavigationManager.getInstance().fragmentManager, "LoadingDialog")
        val currentTest = Study.getCurrentTestSession()
        currentTest.markCompleted()
        setTestCompleteFlag(true)
        loadTestDataFromCache()
        save()
        submitTest(Study.getCurrentTestSession())
        dialog.dismiss()
    }

    override fun setupPath() {
        Log.i(tag, "setupPath")
        Log.i(tag, "path = " + getPathName(state.currentPath))
        when (state.currentPath) {
            PATH_TEST_FIRST_OF_BASELINE -> setPathFirstOfBaseline()
            PATH_TEST_BASELINE -> setPathBaselineTest()
            PATH_TEST_FIRST_OF_DAY -> setPathTestFirstOfDay()
            PATH_TEST_FIRST_OF_VISIT -> setPathTestFirstOfVisit()
            PATH_TEST_OTHER -> setPathTestOther()
            PATH_TEST_NONE -> setPathNoTests()
            PATH_STUDY_OVER -> setPathOver()
            else -> setPathNoTests() // do nothing
        }
    }
}

enum class ArcTestState {
    SIGNATURE_START,
    CHRONOTYPE, WAKE, CONTEXT,
    PRICES, SYMBOLS, GRID,
    THANK_YOU_TODAY, THANK_YOU_CYCLE, THANK_YOU_COMPLETE
}