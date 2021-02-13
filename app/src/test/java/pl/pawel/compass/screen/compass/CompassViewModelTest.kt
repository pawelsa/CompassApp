package pl.pawel.compass.screen.compass

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import org.junit.Rule
import org.junit.Test
import pl.pawel.compass.data.Location

class CompassViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val viewModel = CompassViewModel()

    @Test
    fun `should be empty at init`() {
        viewModel.state.test().assertNoValue()
    }

    @Test
    fun `should update compass bearing`() {
        val testObserver = viewModel.state
            .test()

        viewModel.updateRotation(-30f)

        testObserver
            .map { it.bearing }
            .assertValue(30f)

    }

    @Test
    fun `should update my localization`() {
        val testObserver = viewModel.state
            .test()

        viewModel.updateRotation(-30f)
        viewModel.updateMyLocation(Location(40f, 40f))

        testObserver
            .map { it is CompassState.OnlyCompass }
            .assertValue(true)
        testObserver
            .map { it.bearing }
            .assertValue(30f)
    }

    @Test
    fun `should update bearing and destination`() {
        val testObserver = viewModel.state
            .test()

        viewModel.updateRotation(-30f)
        viewModel.updateMyLocation(Location(40f, 40f))
        viewModel.updateDestination(Location(50f, 50f))

        testObserver
            .map { it is CompassState.CompassWithLocalizationState }
            .assertValue(true)
        testObserver
            .map { it.bearing }
            .assertValue(30f)

    }
}