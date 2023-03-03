package edu.wustl.Arc.app.arc.model

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.text.Spanned
import android.util.Log
import androidx.core.text.HtmlCompat
import androidx.core.text.toSpanned
import edu.wustl.arc.core.ArcApplication
import edu.wustl.arc.core.ArcBaseFragment
import edu.wustl.arc.core.Config
import edu.wustl.arc.core.LoadingDialog
import edu.wustl.arc.core.SimplePopupScreen
import edu.wustl.arc.navigation.NavigationManager
import edu.wustl.arc.path_data.Grid2TestPathData
import edu.wustl.arc.path_data.GridTestPathData
import edu.wustl.arc.path_data.PriceTestPathData
import edu.wustl.arc.path_data.SymbolsTestPathData
import edu.wustl.arc.paths.templates.TestInfoTemplate
import edu.wustl.arc.paths.tests.Grid2Letters
import edu.wustl.arc.paths.tests.Grid2Study
import edu.wustl.arc.paths.tests.Grid2Test
import edu.wustl.arc.paths.tests.GridLetters
import edu.wustl.arc.paths.tests.GridStudy
import edu.wustl.arc.paths.tests.GridTest
import edu.wustl.arc.paths.tests.PriceTestCompareFragment
import edu.wustl.arc.paths.tests.PriceTestMatchFragment
import edu.wustl.arc.paths.tests.SymbolTest
import edu.wustl.arc.paths.tests.TestBegin
import edu.wustl.arc.paths.tests.TestProgress
import edu.wustl.arc.study.PathSegment
import edu.wustl.arc.study.StateMachineAlpha
import edu.wustl.arc.study.Study
import edu.wustl.arc.study.TestSession
import edu.wustl.arc.study.TestVariant
import edu.wustl.arc.utilities.PriceManager
import edu.wustl.arc.utilities.ViewUtil
import org.sagebionetworks.research.mobiletoolbox.app.R
import org.sagebionetworks.research.mobiletoolbox.app.arc.ArcAssessmentType
import org.sagebionetworks.research.mobiletoolbox.app.arc.ArcTestInfoStepObject
import java.io.File
import java.io.FileOutputStream

public interface SessionCompleteListener {
    fun onSessionComplete(signatureList: ArrayList<File>, session: TestSession)
}

open class ArcStateMachine : StateMachineAlpha() {

    companion object {
        const val jpegQuality = 80

        @SuppressLint("DiscouragedApi")
        fun localizeText(context: Context, localizationKey: String?): Spanned? {
            if (localizationKey == null) {
                return null
            }
            val res = context.resources
            return try {
                HtmlCompat.fromHtml(
                    res.getString(res.getIdentifier(localizationKey,
                        "string", context.getPackageName())),
                    HtmlCompat.FROM_HTML_MODE_LEGACY)
            } catch (exception: Resources.NotFoundException) {
                localizationKey?.toSpanned()
            }
        }
    }

    var listener: SessionCompleteListener? = null
    private val signatureList: ArrayList<File> = arrayListOf()

    override fun initialize() {
        super.initialize()
        state.lifecycle = LIFECYCLE_INIT
        state.currentPath = PATH_TEST_NONE
    }

    fun addPricesTest(context: Context, step: ArcTestInfoStepObject) {
        val fragments: MutableList<ArcBaseFragment?> = mutableListOf()
        val info = TestInfoTemplate(
            " ",
            ViewUtil.getHtmlString(R.string.prices_header),
            ViewUtil.getHtmlString(R.string.prices_body),
            ArcAssessmentType.PRICES.toIdentifier(),
            ViewUtil.getHtmlString(R.string.button_begintest))
        fragments.add(info)
        fragments.add(TestBegin())
        val size = PriceManager.getInstance().priceSet.size

        for (i in 0 until size) {
            fragments.add(PriceTestCompareFragment(i))
        }

        fragments.add(
            SimplePopupScreen(
                ViewUtil.getHtmlString(R.string.prices_overlay),
                ViewUtil.getHtmlString(R.string.button_begin),
                3000L,
                15000L,
                false
            )
        )
        fragments.add(PriceTestMatchFragment())

        val segment = PathSegment(
            fragments,
            PriceTestPathData::class.java)
        cache.segments.add(segment)
    }

    fun addGridTest(context: Context, step: ArcTestInfoStepObject) {
        when (Config.TEST_VARIANT_GRID) {
            TestVariant.Grid.V1 -> addGrid1Test(context, step)
            else -> addGrid2Test(context, step) // TestVariant.Grid.V2
        }
    }

    fun addGrid1Test(context: Context, step: ArcTestInfoStepObject) {
        val fragments: MutableList<ArcBaseFragment> = mutableListOf()
        val info0 = TestInfoTemplate(
            " ",
            ViewUtil.getHtmlString(R.string.grids_header),
            ViewUtil.getHtmlString(R.string.grids_body),
            ArcAssessmentType.GRIDS.toIdentifier(),
            ViewUtil.getHtmlString(R.string.button_begintest))
        fragments.add(info0)
        fragments.add(TestBegin())
        fragments.add(GridStudy())
        fragments.add(GridLetters())
        fragments.add(GridTest())
        fragments.add(GridStudy())
        fragments.add(GridLetters())
        val gridTestFragment = GridTest()
        gridTestFragment.second = true
        fragments.add(gridTestFragment)
        val segment = PathSegment(
            fragments,
            GridTestPathData::class.java
        )
        enableTransitionGrids(segment, true)
        cache.segments.add(segment)
    }

    fun addGrid2Test(context: Context, step: ArcTestInfoStepObject) {
        val fragments: MutableList<ArcBaseFragment?> = mutableListOf()
        val info0 = TestInfoTemplate(
            " ",
            ViewUtil.getHtmlString(R.string.grids_header),
            ViewUtil.getHtmlString(R.string.grids_vb_body),
            ArcAssessmentType.GRIDS.toIdentifier(),
            ViewUtil.getHtmlString(R.string.button_begintest))
        fragments.add(info0)
        fragments.add(TestBegin())
        fragments.add(Grid2Study())
        fragments.add(Grid2Letters())
        fragments.add(Grid2Test())
        fragments.add(Grid2Study())
        fragments.add(Grid2Letters())
        fragments.add(Grid2Test())
        val segment = PathSegment(
            fragments,
            Grid2TestPathData::class.java)
        enableTransitionGrids(segment, true)
        cache.segments.add(segment)
    }

    fun addSymbolsTest(context: Context, step: ArcTestInfoStepObject) {
        val fragments: MutableList<ArcBaseFragment?> = mutableListOf()

        // This is the first intro screen of the ARC assessments, so initialize the data collection
        (Study.getStateMachine() as? ArcStateMachine)?.initializeData(step.testType)

        val infoTemplateType = step.testType.toIdentifier()

        val info = TestInfoTemplate(
            " ",
            ViewUtil.getHtmlString(R.string.symbols_header),
            ViewUtil.getHtmlString(R.string.symbols_body),
            infoTemplateType,
            ViewUtil.getHtmlString(R.string.button_begintest))

        fragments.add(info)
        fragments.add(TestBegin())
        fragments.add(SymbolTest())
        fragments.add(TestProgress(ViewUtil.getString(R.string.symbols_complete), 0))

        val segment = PathSegment(
            fragments,
            SymbolsTestPathData::class.java)
        cache.segments.add(segment)
    }

    override fun moveOn(): Boolean {
        cache.data.clear()
        decidePath()
        setupPath()
        Study.getInstance()
        Study.getParticipant().save()
        this.save()
        return false
    }

    fun initializeData(assessmentType: ArcAssessmentType) {
        val segment = PathSegment()
        segment.dataObject = when(assessmentType) {
            ArcAssessmentType.SYMBOLS -> SymbolsTestPathData()
            ArcAssessmentType.PRICES -> PriceTestPathData()
            else -> GridTestPathData()
        }
        cache.segments.clear()
        cache.segments.add(segment)
    }

    override fun submitSignature(signature: Bitmap?) {
        val sigUnwrapped = signature ?: run { return }

        val signatureDir = ArcApplication.getInstance()
            .appContext.filesDir.resolve("signatures")
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