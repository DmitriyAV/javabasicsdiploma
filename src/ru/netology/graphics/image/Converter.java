package ru.netology.graphics.image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Converter implements TextGraphicsConverter {

    private int sourceWidth;
    private int sourceHeight;
    private int targetWidth;
    private int targetHeight;
    private double sourceRatio;
    private double targetRatio;
    private char currentChar;

    @Override
    public String convert(String url) throws IOException, BadImageSizeException {
        // Вот так просто мы скачаем картинку из интернета :)
        BufferedImage img = ImageIO.read(new URL(url));
        sourceHeight = img.getHeight();
        sourceWidth = img.getWidth();
        sourceRatio = setRatio();
        // Если конвертер попросили проверять на максимально допустимое
        // соотношение сторон изображения, то вам здесь надо сделать эту проверку,
        // и, если картинка не подходит, выбросить исключение BadImageSizeException.
        // Чтобы получить ширину картинки, вызовите img.getWidth(), высоту - img.getHeight()
        checkRatio();
        // Если конвертеру выставили максимально допустимые ширину и/или высоту,
        // вам надо по ним и по текущим высоте и ширине вычислить новые высоту
        // и ширину.
        int newWidth = getTargetWidth();
        int newHeight = getTargetHeight();
        // Теперь нам надо попросить картинку изменить свои размеры на новые
        // Последний параметр означает, что мы просим картинку плавно сузиться
        // на новые размеры. В результате мы получаем ссылку на новую картинку, которая
        // представляет собой суженную старую.
        Image scaledImage = img.getScaledInstance(newWidth, newHeight, BufferedImage.SCALE_SMOOTH);
        // Теперь сделаем её чёрно-белой. Для этого поступим так:
        // Создадим новую пустую картинку нужных размеров, заранее указав последним
        // параметром чёрно-белую цветовую палитру:
        BufferedImage bwImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
        // Попросим у этой картинки инструмент для рисования на ней:
        Graphics2D graphics = bwImg.createGraphics();
        // А этому инструменту скажем, чтобы он скопировался из нашей суженной картинки:
        graphics.drawImage(scaledImage, 0, 0, null);
        // Теперь в bwImg у нас лежит чёрно-белая картинка нужных нам размеров.
        // Вы можете отслеживать каждый из этапов, просто в любом удобном для
        // вас моменте сохранив промежуточную картинку в файл через:
        ImageIO.write(bwImg, "png", new File("out.png"));
        // После вызова этой инструкции у вас в проекте появится файл картинки out.png
        // Теперь давайте пройдёмся по пикселям нашего изображения.
        // Если для рисования мы просили у картинки .createGraphics(),
        // то для прохода по пикселям нам нужен будет этот инструмент:
        var bwRaster = bwImg.getRaster();
        // Он хорош тем, что у него мы можем спросить пиксель на нужных
        // нам координатах, указав номер столбца (w) и строки (h)
        // int color = bwRaster.getPixel(w, h, new int[3])[0];
        // Выглядит странно? Согласен. Сам возвращаемый методом пиксель это
        // массив из трёх интов, обычно это интенсивность красного, зелёного и синего.
        // Но у нашей чёрно-белой картинки цветов нет и нас интересует
        // только первое значение в массиве. Вы спросите, а зачем
        // мы ещё параметром передаём интовый массив на три ячейки?
        // Дело в том что этот метод не хочет создавать его сам и просит
        // вас сделать это, а сам метод лишь заполнит его и вернёт.
        // Потому что создавать массивы каждый раз это медленно. Вы можете создать
        // массив один раз, сохранить в переменную и передавать один
        // и тот же массив в метод, ускорив тем самым программу.
        // Вам осталось пробежаться двойным циклом по всем столбцам (ширина)
        // и строкам (высота) изображения, на каждой внутренней итерации
        // получить степень белого пикселя (int color выше) и по ней
        // получить соответствующий символ c. Логикой превращения цвета
        // в символ будет заниматься другой объект, который мы рассмотрим ниже
        TextColorSchema schema = color -> {
            //  char p = '#', p1 = '$', p2 = '@', p3 = '%', p4 = '*', p5 = '+', p6 = '-', p7 = '\'';
            char[] digits = {'#', '$', '@', '%', '*', '+', '-', '\''};
            for (int i = 0; i < digits.length; i++) {
                if (new Range(0, 31).contains(color)) return currentChar = digits[0];
                else if (new Range(31, 63).contains(color)) return currentChar = digits[1];
                else if (new Range(63, 96).contains(color)) return currentChar = digits[2];
                else if (new Range(96, 130).contains(color)) return currentChar = digits[3];
                else if (new Range(130, 163).contains(color)) return currentChar = digits[4];
                else if (new Range(163, 196).contains(color)) return currentChar = digits[5];
                else if (new Range(196, 229).contains(color)) return currentChar = digits[6];
                else if (new Range(229, 255).contains(color)) return currentChar = digits[7];
            }
            return currentChar;
        };

        char[][] colorChar = new char[newWidth][newHeight];

        for (int w = 0; w < newWidth; w++) {
            for (int h = 0; h < newHeight; h++) {
                int color = bwRaster.getPixel(w, h, new int[3])[0];

                char c = schema.convert(color);
                colorChar[w][h] = c;
                //запоминаем символ c, например, в двумерном массиве

            }
        }

        return print(colorChar);
    }

    private String print(char[][] colorChar) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < colorChar.length; i++) {
            for (int j = 0; j < colorChar[i].length; j++) {
                sb
                        .append(colorChar[i][j])
                        .append('\t');

            }
        }
        return sb.toString();
    }

    private double setRatio() {
        return this.sourceRatio = (double) sourceWidth / sourceHeight;
    }

    private int getTargetHeight() {
        return targetHeight;
    }

    private int getTargetWidth() {
        return targetWidth;
    }

    private void setWidth(int width) {
        this.targetWidth = width;
    }

    private void setHeight(int height) {
        this.targetHeight = height;
    }

    private void checkRatio() throws BadImageSizeException {
        if (sourceRatio > targetRatio) {
            throw new BadImageSizeException(targetRatio, sourceRatio);
        } else System.out.println("ratio " + sourceRatio + " is good");
    }

    private void heightNew() {
        int newHeight;
        if (sourceHeight > targetHeight) {
            double ratio = (double) targetHeight / sourceHeight;

            this.sourceWidth = (int) (sourceWidth * ratio);
            newHeight = (int) (targetHeight * ratio);
            setHeight(newHeight);
        } else System.out.println("Height " + sourceHeight + " is ok");
    }

    private void widthNew() {
        int newWidth;
        if (sourceWidth > targetWidth) {
            double ratio = (double) targetWidth / sourceWidth;

            this.sourceHeight = (int) (sourceHeight * ratio);
            newWidth = (int) (targetWidth * ratio);
            setWidth(newWidth);
        } else System.out.println("Width " + sourceWidth + " is ok");
    }

    @Override
    public void setMaxWidth(int width) {
        this.targetWidth = width;
        widthNew();
    }


    @Override
    public void setMaxHeight(int height) {
        this.targetHeight = height;
        heightNew();
    }

    @Override
    public void setMaxRatio(double maxRatio) throws BadImageSizeException {
        this.targetRatio = maxRatio;
    }

    @Override
    public void setTextColorSchema(TextColorSchema schema) {
        for (int i = 0; i < 255; i++) {
            schema.convert(i);

        }
    }
}
