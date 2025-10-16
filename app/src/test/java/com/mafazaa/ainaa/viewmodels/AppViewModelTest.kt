package com.mafazaa.ainaa.viewmodels


import com.mafazaa.ainaa.data.local.SharedPrefs
import com.mafazaa.ainaa.data.local.add
import com.mafazaa.ainaa.data.local.remove
import com.mafazaa.ainaa.domain.FileRepo
import com.mafazaa.ainaa.domain.models.AppInfo
import com.mafazaa.ainaa.domain.repo.RemoteRepo
import com.mafazaa.ainaa.domain.repo.UpdateRepo
import com.mafazaa.ainaa.helpers.ScreenshotOverlayManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * This rule swaps the main dispatcher with a test dispatcher. This is crucial for testing
 * ViewModels that use viewModelScope, which defaults to Dispatchers.Main.
 *
 * NOTE: It's best practice to move this class into its own file (e.g., MainCoroutineRule.kt).
 */
@ExperimentalCoroutinesApi
class MainCoroutineRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}

@ExperimentalCoroutinesApi
class AppViewModelTest {

    // This rule ensures that LiveData updates happen synchronously.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // This rule handles the coroutine dispatcher for viewModelScope.
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    // Create relaxed mocks for all dependencies. "Relaxed" means we don't have to stub every single function.
    @RelaxedMockK
    private lateinit var remoteRepo: RemoteRepo
    @RelaxedMockK
    private lateinit var sharedPrefs: SharedPrefs
    @RelaxedMockK
    private lateinit var fileRepo: FileRepo
    @RelaxedMockK
    private lateinit var updateRepo: UpdateRepo
    @RelaxedMockK
    private lateinit var screenshotOverlayManager: ScreenshotOverlayManager

    // The ViewModel we are testing.
    private lateinit var viewModel: AppViewModel

    @Before
    fun setUp() {
        // Initialize the mocks defined above.
        MockKAnnotations.init(this)
        // Create a new ViewModel instance before each test with the mocked dependencies.
        viewModel = AppViewModel(
            remoteRepo,
            sharedPrefs,
            fileRepo,
            updateRepo,
            screenshotOverlayManager
        )
    }

    @After
    fun tearDown() {
        // Clean up mocks after each test.
        unmockkAll()
    }

    @Test
    fun `loadInstalledApps should mark apps from sharedPrefs as selected`() = runTest {
        // GIVEN: A list of apps and a preference indicating one app is blocked.
        val appsToLoad = listOf(
            AppInfo("app1", "App One", null, false),
            AppInfo("app2", "App Two", null, false)
        )
        every { sharedPrefs.blockedApps } returns listOf("app2")

        // WHEN: We load the apps into the ViewModel.
        viewModel.loadInstalledApps(appsToLoad)

        // THEN: The ViewModel's app list should correctly mark "app2" as selected.
        val resultApps = viewModel.apps.first() // Get the first emitted value from the StateFlow
        assertFalse(resultApps.find { it.packageName == "app1" }!!.isSelected)
        assertTrue(resultApps.find { it.packageName == "app2" }!!.isSelected)
    }

    @Test
    fun `toggleAppSelection should add an unselected app to blocked list`() = runTest {
        // GIVEN: An app that is not currently selected and an empty list of blocked apps.
        val packageName = "com.test.app"
        viewModel.loadInstalledApps(listOf(AppInfo(packageName, "Test App", null, false)))
        every { sharedPrefs.blockedApps } returns emptyList()

        // WHEN: We toggle the app's selection.
        viewModel.toggleAppSelection(packageName)

        // THEN: The ViewModel should update its internal list and call sharedPrefs to add the app.
        val updatedApps = viewModel.apps.first()
        assertTrue(updatedApps.first().isSelected)
        verify { sharedPrefs.blockedApps = any<List<String>>().add(packageName) }
    }

    @Test
    fun `toggleAppSelection should remove a selected app from blocked list`() = runTest {
        // GIVEN: An app that is already selected and in the blocked apps list.
        val packageName = "com.test.app"
        viewModel.loadInstalledApps(listOf(AppInfo(packageName, "Test App", null, true)))
        every { sharedPrefs.blockedApps } returns listOf(packageName)

        // WHEN: We toggle the app's selection.
        viewModel.toggleAppSelection(packageName)

        // THEN: The ViewModel should update its list and call sharedPrefs to remove the app.
        val updatedApps = viewModel.apps.first()
        assertFalse(updatedApps.first().isSelected)
        verify { sharedPrefs.blockedApps = any<List<String>>().remove(packageName) }
    }

    @Test
    fun `showScreenshotOverlay with true should call manager to show overlay`() {
        // WHEN
        viewModel.showScreenshotOverlay(true)
        
        // THEN
        verify(exactly = 1) { screenshotOverlayManager.showOverlay() }
        verify(exactly = 0) { screenshotOverlayManager.closeOverlay() }
    }
    
    @Test
    fun `showScreenshotOverlay with false should call manager to close overlay`() {
        // WHEN
        viewModel.showScreenshotOverlay(false)
        
        // THEN
        verify(exactly = 1) { screenshotOverlayManager.closeOverlay() }
        verify(exactly = 0) { screenshotOverlayManager.showOverlay() }
    }
}