package net.shchastnyi.medical.tradeexchange

/**
 * Given some string with Cyrillic letters encodes it with Latin ones
 */
object Translit {

  val map: Map[Char, String] = Map(
    'а'->"a",
    'б'->"b",
    'в'->"v",
    'г'->"g",
    'д'->"d",
    'е'->"e",
    'ё'->"yo",
    'ж'->"zh",
    'з'->"z",
    'и'->"i",
    'й'->"j",
    'к'->"k",
    'л'->"l",
    'м'->"m",
    'н'->"n",
    'о'->"o",
    'п'->"p",
    'р'->"r",
    'с'->"s",
    'т'->"t",
    'у'->"u",
    'ф'->"f",
    'х'->"h",
    'ц'->"ts",
    'ч'->"ch",
    'ш'->"sh",
    'щ'->"sh",
    'ъ'->"y",
    'ы'->"y",
    'ь'->"y",
    'э'->"e",
    'ю'->"yu",
    'я'->"ya",
    '«'->"",
    '»'->"",
    '№'->"No",
    ' '->"_"
  )

  def apply(text: String): String = translit(text)

  def translit(text: String): String =
    text.toCharArray.map( (x: Char) => map.getOrElse(x.toLower, x) ).mkString

}
