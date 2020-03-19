package bot.imgur.api

case class Response(data: List[Data])

case class Data(images: List[InnerData])

case class InnerData(link: String)
