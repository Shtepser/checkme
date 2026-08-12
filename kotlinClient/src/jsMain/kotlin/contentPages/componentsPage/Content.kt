package ru.yarsu.contentPages.componentsPage

import io.kvision.panel.SimplePanel
import io.kvision.routing.Routing
import ru.yarsu.contentPages.content.bundlesPages.BundlesList
import ru.yarsu.enumClasses.ListType

class Content(
    serverUrl : String,
    routing: Routing,
) : SimplePanel() {
    init {
        add(BundlesList(serverUrl, routing, ListType.ALL))
    }
}