package mixconfig;

import java.io.File;
   class SimpleFileFilter extends javax.swing.filechooser.FileFilter
      {
        private String m_strDesc;
        private String m_strExtension;
        public SimpleFileFilter(int filter_type)
          {
            switch (filter_type)
              {
                case TheApplet.FILTER_CER:
                  m_strDesc="Public X.509 Certificate (*.cer)";
                  m_strExtension=".cer";
                break;
                case TheApplet.FILTER_XML:
                  m_strDesc="Mix Configuration (*.xml)";
                  m_strExtension=".xml";
                break;
                case TheApplet.FILTER_PFX:
                  m_strDesc="Private Key with Certificate (*.pfx)";
                  m_strExtension=".pfx";
                break;
                default:
                  m_strDesc="";
                  m_strExtension="";
              }

          };

        public boolean accept(File f)
          {
            return f.isDirectory()||f.getName().endsWith(m_strExtension);
          }

        public String getDescription()
          {
            return m_strDesc;
          }
      }
