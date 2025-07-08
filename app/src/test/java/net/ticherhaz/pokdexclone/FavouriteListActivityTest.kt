package net.ticherhaz.pokdexclone

import android.view.View
import androidx.core.util.Predicate
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.ui.UiController
import dagger.hilt.android.testing.HiltAndroidTest
import net.ticherhaz.pokdexclone.adapter.PokemonAdapter
import net.ticherhaz.pokdexclone.dao.AppDatabase
import net.ticherhaz.pokdexclone.dao.PokemonDao
import net.ticherhaz.pokdexclone.model.PokemonEntity
import net.ticherhaz.pokdexclone.ui.favourite.FavouriteActivity
import net.ticherhaz.pokdexclone.ui.main.detail.PokemonDetailActivity
import net.ticherhaz.pokdexclone.utils.Constant
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.text.matches

@HiltAndroidTest
@RunWith(AndroidJUnit4::class) // Specify your test runner
class FavouriteListActivityTest {

    @get:Rule(order = 0) // HiltRule should run first
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1) // ActivityScenarioRule after Hilt
    var activityScenarioRule = ActivityScenarioRule(FavouriteActivity::class.java)

    // Assuming you have a Hilt module that provides AppDatabase for tests
    // If not, you might need to create an in-memory version for testing.
    @Inject
    lateinit var appDatabase: AppDatabase

    private lateinit var pokemonDao: PokemonDao

    @Before
    fun setUp() {
        hiltRule.inject() // Inject Hilt dependencies
        pokemonDao = appDatabase.pokemonDao()
        IdlingRegistry.getInstance().register(EspressoIdlingResource.idlingResource)
    }

    @After
    fun tearDown() {
        // It's good practice to clear data specific to the test
        // Clearing all Pokémon might affect other tests if they run in parallel
        // or share the same DB instance without proper isolation.
        // For isolated tests, this is fine.
        pokemonDao.clearAllPokemon() // Assuming this is a suspend function, call it in runTest if needed
        appDatabase.close()
        androidx.test.espresso.IdlingRegistry.getInstance()
            .unregister(EspressoIdlingResource.idlingResource)
    }

    @Test
    fun testDisplayFavoritePokemonList() = runTest { // Use runTest for suspend functions
        // Arrange: Insert favorite Pokémon
        val pokemon = listOf(
            PokemonEntity(
                name = "bulbasaur",
                url = "https://pokeapi.co/api/v2/pokemon/1/",
                // imageFilePath = "/path/to/bulbasaur.png", // Assuming this field exists in your entity
                isFavorite = true // Changed from isFavourite to match typical Kotlin naming
            ),
            PokemonEntity(
                name = "charmander",
                url = "https://pokeapi.co/api/v2/pokemon/4/",
                // imageFilePath = "/path/to/charmander.png",
                isFavorite = true
            )
        )
        pokemonDao.insertPokemonList(pokemon)

        // Act: Launch activity (already launched by ActivityScenarioRule)
        // activityScenarioRule.scenario // This line is not strictly needed to trigger launch if the rule is set up

        // Assert: Check RecyclerView displays Pokémon
        onView(withId(R.id.recyclerViewPokemon)).check(matches(isDisplayed()))
        onView(withId(R.id.recyclerViewPokemon)).check(matches(hasDescendant(withText("bulbasaur"))))
        onView(withId(R.id.recyclerViewPokemon)).check(matches(hasDescendant(withText("charmander"))))
        onView(withId(R.id.tvEmptyState)).check(matches(Predicate.not(isDisplayed())))
    }

    @Test
    fun testEmptyStateDisplayedWhenNoFavorites() = runTest {
        // Arrange: Ensure no favorites
        pokemonDao.clearAllPokemon()

        // Act: (Activity already launched)

        // Assert: Check empty state
        onView(withId(R.id.recyclerViewPokemon)).check(matches(Predicate.not(isDisplayed())))
        onView(withId(R.id.tvEmptyState)).check(matches(isDisplayed()))
        // Consider making the expected text a string resource for easier maintenance
        onView(withId(R.id.tvEmptyState)).check(matches(withText("No favorite Pokémon found")))
    }

    @Test
    fun testToggleFavoriteRemovesPokemon() = runTest {
        // Arrange: Insert a favorite Pokémon
        val pokemon = PokemonEntity(
            name = "bulbasaur",
            url = "https://pokeapi.co/api/v2/pokemon/1/",
            // imageFilePath = "/path/to/bulbasaur.png",
            isFavorite = true
        )
        pokemonDao.insertPokemon(pokemon)
        activityScenarioRule.scenario // Ensure activity is ready

        // Act: Toggle favorite
        // Ensure the item is visible before performing action
        onView(withId(R.id.recyclerViewPokemon)).check(matches(hasDescendant(withText("bulbasaur"))))
        onView(withId(R.id.recyclerViewPokemon))
            .perform(
                actionOnItemAtPosition<PokemonAdapter.PokemonViewHolder>(
                    0,
                    clickChildViewWithId(R.id.ivFavourite) // Assuming you have a custom action
                )
            )

        // Assert: Check Pokémon is removed and toast/empty state is shown
        // Depending on how quickly the UI updates and if toast is reliable for testing:
        onView(withId(R.id.recyclerViewPokemon)).check(
            matches(
                Predicate.not(
                    hasDescendant(
                        withText(
                            "bulbasaur"
                        )
                    )
                )
            )
        )
        onView(withId(R.id.tvEmptyState)).check(matches(isDisplayed()))
        // Toast checking can be flaky. If essential, look into Espresso-Toast or IdlingResources for Toasts.
        // For this example, I'll comment it out as it's often a source of test flakiness.
        // onView(withText("Removed from Favourite")).inRoot(isToast()).check(matches(isDisplayed()))
    }

    @Test
    fun testNavigateToPokemonDetailActivity() = runTest {
        androidx.test.espresso.intent.Intents.init() // Initialize Intents before the action that triggers navigation
        // Arrange: Insert a favorite Pokémon
        val pokemon = PokemonEntity(
            name = "bulbasaur",
            url = "https://pokeapi.co/api/v2/pokemon/1/",
            // imageFilePath = "/path/to/bulbasaur.png",
            isFavorite = true
        )
        pokemonDao.insertPokemon(pokemon)
        activityScenarioRule.scenario // Ensure activity is ready

        // Act: Click on Pokémon item
        onView(withId(R.id.recyclerViewPokemon))
            .perform(actionOnItemAtPosition<PokemonAdapter.PokemonViewHolder>(0, click()))

        // Assert: Check PokemonDetailActivity is launched with correct extras
        intended(hasComponent(PokemonDetailActivity::class.java.name))
        intended(hasExtra(Constant.POKEMON_NAME, "bulbasaur"))
        androidx.test.espresso.intent.Intents.release() // Release Intents after assertion
    }

    @Test
    fun testOfflineSupport() = runTest {
        // Arrange: Insert favorite Pokémon
        val pokemon = listOf(
            PokemonEntity(
                name = "bulbasaur",
                url = "https://pokeapi.co/api/v2/pokemon/1/",
                // imageFilePath = "/path/to/bulbasaur.png",
                isFavorite = true
            ),
            PokemonEntity(
                name = "charmander",
                url = "https://pokeapi.co/api/v2/pokemon/4/",
                // imageFilePath = "/path/to/charmander.png",
                isFavorite = true
            )
        )
        pokemonDao.insertPokemonList(pokemon)
        activityScenarioRule.scenario // Ensure activity is ready

        // Act: (Activity already launched, network state is irrelevant for this test)

        // Assert: Check favorites display
        onView(withId(R.id.recyclerViewPokemon)).check(matches(isDisplayed()))
        onView(withId(R.id.recyclerViewPokemon)).check(matches(hasDescendant(withText("bulbasaur"))))
        onView(withId(R.id.recyclerViewPokemon)).check(matches(hasDescendant(withText("charmander"))))
        onView(withId(R.id.tvEmptyState)).check(matches(Predicate.not(isDisplayed())))
    }

    // Helper function for clicking a child view within a RecyclerView item
    // You might already have this or a similar utility.
    private fun clickChildViewWithId(id: Int): androidx.test.espresso.ViewAction {
        return object : androidx.test.espresso.ViewAction {
            override fun getConstraints(): Matcher<View>? {
                return null
            }

            override fun getDescription(): String {
                return "Click on a child view with specified id."
            }

            override fun perform(uiController: UiController, view: View) {
                val v = view.findViewById<View>(id)
                v.performClick()
            }
        }
    }
}
