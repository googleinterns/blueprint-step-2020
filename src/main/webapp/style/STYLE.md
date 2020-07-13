#CSS Style Methodologies
Overall, this project uses some style patterns that are typical of SASS.
While the benefits of using these patterns slightly deminishes since the project only
uses vanilla CSS, there are two distinct benefits to doing this:

1. Following an established naming pattern & file hierarchy is helpful. It will keep the
project's stylesheets organized and make it easy to understand how to change/update something

2. This will also make it easier to eventually convert this project over to [SASS](https://sass-lang.com/). 
Valid CSS is also valid SASS, and SASS offers more power and flexibility, which will be helpful for 
future iterations of this project. 

##File Hierarchy
This project uses the [7:1 Pattern](https://www.learnhowtoprogram.com/user-interfaces/building-layouts-preprocessors/7-1-sass-architecture)

This pattern sorts CSS stylesheets into 7 main folders. Note that only some of these folders are currently present.
These folders should be created when useful/needed.

1. `abstracts` should contain no actual styles. Rather, this folder should contain mechanisms to make creating other 
styles easier. This could include utilities or variables.

2. `vendors` contains any 3rd party stylesheets used (e.g. bootstrap)

3. `base` contains boilerplate styles used throughout the site, including typography or stylistic resets.

4. `components` are mini-layouts which format reusable components throughout the project (e.g. panels)

5. `layout` contains style for key structural elements of the site (e.g. navigation bars, headers, footers)

6. `pages` is where page specific styles will reside. This should only be created if this app becomes multi-page

7. `themes` is used to describe any styles specific to different themes of the website (think light vs. dark mode)

All files in these folders begin with underscores. This has no functional benefit in CSS, but makes imports easier in 
SASS. 

##Property Naming Patterns
Classes are named using the [BEM](http://getbem.com/naming/) (Block - Element - Modifier) pattern.

The naming pattern looks like this: `block__element--modifier`.

For example, `panel__content-entry--gmail` would refer to a content-entry in the panel component, with some
specific settings for the gmail panel. 

This pattern keeps things organized in CSS, though it is often critiqued for being overly verbose. In SASS, 
this effect is amplified further, as the & operator allows for nested styles.