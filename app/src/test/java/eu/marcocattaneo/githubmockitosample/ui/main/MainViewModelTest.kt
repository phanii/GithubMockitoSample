package eu.marcocattaneo.githubmockitosample.ui.main

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Observer
import eu.marcocattaneo.githubmockitosample.data.Repository
import eu.marcocattaneo.githubmockitosample.data.UserService
import eu.marcocattaneo.githubmockitosample.utils.LiveDataResult
import io.reactivex.Maybe
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.net.SocketException

@RunWith(JUnit4::class)
class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var userService: UserService

    lateinit var mainViewModel: MainViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        this.mainViewModel = MainViewModel(this.userService)
    }

    @Test
    fun fetchRepositories_positiveResponse() {
        Mockito.`when`(this.userService.getRepositories(ArgumentMatchers.anyString())).thenAnswer {
            return@thenAnswer Maybe.just(ArgumentMatchers.anyList<Repository>())
        }

        val observer = mock(Observer::class.java) as Observer<LiveDataResult<List<Repository>>>
        this.mainViewModel.repositoriesLiveData.observeForever(observer)

        this.mainViewModel.fetchUserRepositories(ArgumentMatchers.anyString())

        assertNotNull(this.mainViewModel.repositoriesLiveData.value)
        assertEquals(LiveDataResult.Status.SUCCESS, this.mainViewModel.repositoriesLiveData.value?.status)
    }

    @Test
    fun fetchRepositories_error() {
        Mockito.`when`(this.userService.getRepositories(ArgumentMatchers.anyString())).thenAnswer {
            return@thenAnswer Maybe.error<SocketException>(SocketException("No network here"))
        }

        val observer = mock(Observer::class.java) as Observer<LiveDataResult<List<Repository>>>
        this.mainViewModel.repositoriesLiveData.observeForever(observer)

        this.mainViewModel.fetchUserRepositories(ArgumentMatchers.anyString())

        assertNotNull(this.mainViewModel.repositoriesLiveData.value)
        assertEquals(LiveDataResult.Status.ERROR, this.mainViewModel.repositoriesLiveData.value?.status)
        assert(this.mainViewModel.repositoriesLiveData.value?.err is Throwable)
    }

    @Test
    fun setLoadingVisibility_onSuccess() {
        Mockito.`when`(this.userService.getRepositories(com.nhaarman.mockitokotlin2.any())).thenAnswer {
            return@thenAnswer Maybe.just(listOf<Repository>())
        }

        val spiedViewModel = com.nhaarman.mockitokotlin2.spy(this.mainViewModel)

        spiedViewModel.fetchUserRepositories(ArgumentMatchers.anyString())
        verify(spiedViewModel, times(2)).setLoadingVisibility(ArgumentMatchers.anyBoolean())
    }

    @Test
    fun setLoadingVisibility_onError() {
        Mockito.`when`(this.userService.getRepositories(com.nhaarman.mockitokotlin2.any())).thenAnswer {
            return@thenAnswer Maybe.error<SocketException>(SocketException())
        }

        val spiedViewModel = com.nhaarman.mockitokotlin2.spy(this.mainViewModel)

        spiedViewModel.fetchUserRepositories(ArgumentMatchers.anyString())
        verify(spiedViewModel, times(2)).setLoadingVisibility(ArgumentMatchers.anyBoolean())
    }

    @Test
    fun setLoadingVisibility_onNoData() {
        Mockito.`when`(this.userService.getRepositories(com.nhaarman.mockitokotlin2.any())).thenReturn(Maybe.create {
            it.onComplete()
        })

        val spiedViewModel = com.nhaarman.mockitokotlin2.spy(this.mainViewModel)

        spiedViewModel.fetchUserRepositories(ArgumentMatchers.anyString())
        verify(spiedViewModel, times(2)).setLoadingVisibility(ArgumentMatchers.anyBoolean())
    }

}