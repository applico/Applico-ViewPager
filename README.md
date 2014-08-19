Applico-ViewPager
=================
This project offers an alternative to the Support Library ViewPager. It is circular and uses the Android 4 Fragment class rather than the Support Library Fragment.

The FragmentManager is used to transparently cache fragments in the view pager class (or in the Slider Example Activity with buttons) instead of replacing and recreating their views with every transition. It animates transitions with object animators. There were issues using the view pager as well as Fragment transition animations. Animations were executed only when a fragment was added, not when an existing fragment is told to hide or show. The circular view pager needs to animate all transitions.

Note that the FragmentManager loses the cache of fragments when the activity leaves the screen.

Because the existing ViewPager requires the support library, hides fragment transactions and hard codes calls to GC - this alternative was created and it is FAST. I hope you like it too!