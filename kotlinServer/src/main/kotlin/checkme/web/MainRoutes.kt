package checkme.web

import checkme.config.AppConfig
import checkme.domain.operations.OperationHolder
import checkme.domain.tools.JWTTools
import checkme.web.admin.ADMIN_SEGMENT
import checkme.web.admin.adminRoutes
import checkme.web.auth.AUTH_SEGMENT
import checkme.web.auth.authRouter
import checkme.web.bundles.BUNDLE_SEGMENT
import checkme.web.bundles.bundleRouter
import checkme.web.context.ContextTools
import checkme.web.filters.FiltersHolder
import checkme.web.solution.SOLUTION_SEGMENT
import checkme.web.solution.solutionRouter
import checkme.web.tasks.TASK_SEGMENT
import checkme.web.tasks.taskRouter
import checkme.web.user.USER_SEGMENT
import checkme.web.user.userRouter
import org.http4k.core.*
import org.http4k.routing.*

private fun createMainRouter(
    operations: OperationHolder,
    config: AppConfig,
    jwtTools: JWTTools,
    contextTools: ContextTools,
) = routes(
    AUTH_SEGMENT bind authRouter(
        config = config,
        operations = operations,
        jwtTools = jwtTools,
        contextTools = contextTools
    ),
    SOLUTION_SEGMENT bind solutionRouter(
        operations = operations,
        contextTools = contextTools,
        config = config
    ),
    TASK_SEGMENT bind taskRouter(
        operations = operations,
        contextTools = contextTools
    ),
    BUNDLE_SEGMENT bind bundleRouter(
        operations = operations,
        contextTools = contextTools
    ),
    ADMIN_SEGMENT bind adminRoutes(
        config = config,
        operations = operations,
        contextTools = contextTools
    ),
    USER_SEGMENT bind userRouter(
        config = config,
        operations = operations,
        contextTools = contextTools
    )
)

fun createApp(
    operations: OperationHolder,
    config: AppConfig,
    contextTools: ContextTools,
): RoutingHttpHandler {
    val jwtTools = JWTTools(config.authConfig.secret, "checkme")
    val filters = FiltersHolder(
        contextTools = contextTools,
        operations = operations,
        jwtTools = jwtTools,
        config = config
    )
    val app = filters.all
        .then(
            createMainRouter(
                operations = operations,
                config = config,
                jwtTools = jwtTools,
                contextTools = contextTools,
            )
        )
    return app
}

val internalServerError: Response = Response(Status.INTERNAL_SERVER_ERROR)

fun ok(body: String): Response = Response(Status.OK).body(body)
